package de.fb.autoregister.commandsystem;

import de.fb.autoregister.commandsystem.slashcommands.SlashCommandRegistrar;
import de.fb.autoregister.commandsystem.slashcommands.subcommands.SubCommandRegistrar;
import de.fb.autoregister.commandsystem.tasksystem.TaskRegistrar;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Central orchestrator for the command and logic registration system.
 * <p>
 * The {@code AnnotationScanner} serves as the entry point for bootstrapping the framework.
 * It performs a single, comprehensive classpath scan and delegates the discovered metadata
 * to a list of specialized {@link FeatureRegistrar} implementations.
 * </p>
 * <p>
 * This design ensures that the expensive reflection process is only executed once,
 * sharing the results across all modules (Slash Commands, Subcommands, etc.).
 * </p>
 * <b>Lifecycle Management:</b>
 * <ul>
 * <li>{@link #registerFeatures()} starts the scanning and registration process.</li>
 * <li>{@link #shutdownFeatures()} ensures a graceful exit by cleaning up all registered modules.</li>
 * </ul>
 */
public class AnnotationScanner {

    private final JDA jda;

    private final Logger logger = LoggerFactory.getLogger(AnnotationScanner.class);

    /** * List of active registrars that define the bot's capabilities.
     * To extend the framework (e.g., with a TaskSystem), add new implementations here.
     */
    private final List<FeatureRegistrar> features = List.of(
            new SubCommandRegistrar(),
            new SlashCommandRegistrar(),
            new TaskRegistrar()
    );

    /**
     * Creates a new AnnotationScanner for the given JDA instance.
     * @param jda The active Discord API connection.
     */
    public AnnotationScanner(JDA jda){
        this.jda = jda;
    }

    /**
     * Bootstraps the framework by scanning the classpath and initializing all features.
     * <p>
     * It uses {@link Scanners#MethodsAnnotated} and {@link Scanners#TypesAnnotated} to
     * capture both standalone commands and command groups. The resulting {@link Reflections}
     * instance is passed to all registrars.
     * </p>
     */
    public void registerFeatures(){
        Reflections methodReflections = new Reflections(new ConfigurationBuilder()
                .forPackage("") // Scans the entire project scope
                .addScanners(Scanners.MethodsAnnotated)
                .addScanners(Scanners.TypesAnnotated)
        );

        features.forEach(featureRegistrar -> featureRegistrar.register(jda, methodReflections));

       List<SlashCommandData> allCommands =  features.stream()
               .flatMap(f -> f.getCommands().stream())
               .collect(Collectors.toList());

       jda.updateCommands().addCommands(allCommands).queue(
               success -> logger.info("successfully registered {} commands", allCommands.size()),
               error -> logger.error("Failed to register Commands", error)
       );
    }

    /**
     * Triggers a graceful shutdown for all registered feature modules.
     * <p>
     * This method iterates through all registrars and invokes their shutdown logic
     * to detach listeners and clear memory, preventing orphaned resources.
     * </p>
     */
    public void shutdownFeatures(){
        features.forEach(featureRegistrar -> featureRegistrar.shutdown(jda));
    }
}