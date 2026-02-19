package de.fb.trackbot.commandsystem.slashcommands.subcommands;

import de.fb.trackbot.commandsystem.CommandParserUtil;
import de.fb.trackbot.commandsystem.FeatureRegistrar;
import de.fb.trackbot.commandsystem.slashcommands.Option;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for hierarchical Discord Slash Commands (Subcommands).
 * <p>
 * This class implements {@link FeatureRegistrar} to identify classes annotated with
 * {@link SlashCommandGroup} and their respective methods annotated with {@link SubCommand}.
 * It automatically assembles the command hierarchy and registers it to the Discord API.
 * Incoming interaction events are routed to the specific methods using a path-based lookup.
 * </p>
 * <b>Requirements:</b>
 * <ul>
 * <li>Classes must be annotated with {@link SlashCommandGroup}.</li>
 * <li>Methods must be annotated with {@link SubCommand}.</li>
 * <li>Methods must return {@code void} and accept exactly one {@link SlashCommandInteractionEvent}.</li>
 * <li>The declaring group class must have a public no-args constructor.</li>
 * </ul>
 * <b>Lifecycle Management:</b>
 * Use {@link #register(JDA, Reflections)} to initialize the hierarchy and
 * {@link #shutdown(JDA)} to detach listeners and release internal caches.
 */
public class SubCommandRegistrar implements FeatureRegistrar {

    private final Logger logger = LoggerFactory.getLogger(SubCommandRegistrar.class);

    /** Map storing unique command paths (group/subcommand) linked to their executable Methods. */
    private final Map<String, Method> subCommandMethods = new ConcurrentHashMap<>();

    /** Map storing command paths linked to the instance of the class containing the method. */
    private final Map<String, Object> commandInstances = new ConcurrentHashMap<>();

    /** Internal listener for intercepting and routing subcommand interactions. */
    private final SubCommandListener listener = new SubCommandListener();

    /** Ensures that the listener for this registrar instance only gets registered once. */
    private boolean isRegistered = false;

    /**
     * Scans the project for {@link SlashCommandGroup} and {@link SubCommand} annotations.
     * The resulting command trees are built and synchronized globally with Discord.
     *
     * @param jda         The active JDA instance for command registration.
     * @param reflections The pre-scanned reflection metadata.
     * @throws IllegalStateException If the registrar is already initialized.
     */
    @Override
    public void register(JDA jda, Reflections reflections) {
        if (isRegistered) {
            throw new IllegalStateException("Subcommands for this registrar instance can only be registered once");
        }

        logger.debug("Starting global scan for SlashCommandGroups and SubCommands");

        Set<Class<?>> groupClasses = reflections.getTypesAnnotatedWith(SlashCommandGroup.class);
        List<SlashCommandData> groupDataList = new ArrayList<>();

        for (Class<?> clazz : groupClasses) {
            groupDataList.add(parseGroup(clazz));
        }

        jda.updateCommands().addCommands(groupDataList).queue(
                success -> logger.info("Successfully registered {} Subcommand Groups", groupDataList.size()),
                error -> logger.error("JDA update for Subcommands failed", error)
        );

        jda.addEventListener(listener);
        isRegistered = true;
    }

    /**
     * Performs a clean shutdown of the subcommand registrar.
     * <p>
     * Detaches the {@link SubCommandListener} and clears all internal method
     * and instance mappings to ensure a leak-free lifecycle.
     * </p>
     *
     * @param jda The JDA instance to remove the listener from.
     */
    @Override
    public void shutdown(JDA jda) {
        logger.info("Shutting down SubCommandRegistrar");
        if (jda != null) {
            jda.removeEventListener(listener);
            logger.debug("SubCommandListener removed successfully");
        }
        subCommandMethods.clear();
        commandInstances.clear();
        isRegistered = false;
        logger.info("SubCommandRegistrar stopped successfully");
    }

    /**
     * Parses a single group class, instantiates it, and maps all contained subcommands.
     *
     * @param clazz The class annotated with {@link SlashCommandGroup}.
     * @return The fully constructed {@link SlashCommandData} including all subcommands.
     */
    private SlashCommandData parseGroup(Class<?> clazz) {
        SlashCommandGroup groupAnno = clazz.getAnnotation(SlashCommandGroup.class);
        String groupName = groupAnno.name().toLowerCase();

        SlashCommandData groupData = Commands.slash(groupName, groupAnno.description());

        try {
            // Instantiate group class (requires no-args constructor)
            Object instance = clazz.getDeclaredConstructor().newInstance();

            for (Method method : clazz.getDeclaredMethods()) {
                if (method.isAnnotationPresent(SubCommand.class)) {

                    CommandParserUtil.validateMethod(method);

                    SubCommand subAnno = method.getAnnotation(SubCommand.class);
                    String subName = subAnno.name().toLowerCase();

                    SubcommandData subData = new SubcommandData(subName, subAnno.description());

                    // Utilize central utility for option parsing
                    Option[] options = subAnno.options();
                    subData.addOptions(CommandParserUtil.parseOptions(options));

                    groupData.addSubcommands(subData);

                    // Map the hierarchical path for the listener (e.g., "settings/volume")
                    String fullPath = groupName + "/" + subName;
                    subCommandMethods.put(fullPath, method);
                    commandInstances.put(fullPath, instance);

                    logger.debug("Mapped SubCommand: {}", fullPath);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize SubCommandGroup: " + clazz.getName(), e);
        }
        return groupData;
    }

    /**
     * Internal listener that routes incoming subcommand interactions to their
     * respective reflected methods using the "group/subcommand" path.
     */
    private class SubCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            // Early exit if this is not a subcommand interaction
            if (event.getSubcommandName() == null) return;

            String path = event.getName() + "/" + event.getSubcommandName();
            Method method = subCommandMethods.get(path);
            Object instance = commandInstances.get(path);

            if (method != null && instance != null) {
                try {
                    method.invoke(instance, event);
                } catch (Exception e) {
                    logger.error("Error executing subcommand: {}", path, e);
                }
            }
        }
    }
}