package de.fb.trackbot.commandsystem;

import de.fb.trackbot.commandsystem.slashcommands.SlashCommandRegistrar;
import de.fb.trackbot.commandsystem.slashcommands.subcommands.SubCommandRegistrar;
import net.dv8tion.jda.api.JDA;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.List;

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

    /** * List of active registrars that define the bot's capabilities.
     * To extend the framework (e.g., with a TaskSystem), add new implementations here.
     */
    private final List<FeatureRegistrar> features = List.of(
            new SlashCommandRegistrar(),
            new SubCommandRegistrar()
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