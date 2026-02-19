package de.fb.trackbot.commandsystem.slashcommands;

import de.fb.trackbot.commandsystem.CommandParserUtil;
import de.fb.trackbot.commandsystem.FeatureRegistrar;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Automated registry for Discord Slash Commands.
 * <p>
 * This class implements {@link FeatureRegistrar} to identify methods annotated with
 * {@link SlashCommand} via reflection and register them globally. It manages the
 * lifecycle of slash command interactions, including registration, event routing,
 * and cleanup.
 * </p>
 * * <b>Requirements:</b>
 * <ul>
 * <li>Methods must be annotated with {@code @SlashCommand}.</li>
 * <li>Methods must be {@code public} (for reflection access).</li>
 * <li>Methods must return {@code void} and accept exactly one {@link SlashCommandInteractionEvent}.</li>
 * <li>Declaring classes must provide a public no-args constructor.</li>
 * </ul>
 * * <b>Lifecycle Management:</b>
 * Use {@link #register(JDA, Reflections)} to initialize and {@link #shutdown(JDA)}
 * to detach listeners and clear internal caches.
 */
public class SlashCommandRegistrar implements FeatureRegistrar {

    private final Logger logger = LoggerFactory.getLogger(SlashCommandRegistrar.class);

    /** Map storing command names linked to their executable Methods. */
    private final Map<String, Method> commandMethods = new ConcurrentHashMap<>();

    /** Map storing command names linked to the instance of the class containing the method. */
    private final Map<String, Object> commandInstances = new ConcurrentHashMap<>();

    /** Internal cache to ensure each class is only instantiated once. */
    private final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();

    /** Ensures that the listener for this scanner instance only gets registered once. */
    private boolean listenerRegistered = false;

    private final SlashCommandListener listener = new SlashCommandListener();

    private List<SlashCommandData> commands = Collections.emptyList();

    /**
     * Scans the project for {@link SlashCommand} annotations and synchronizes them with Discord.
     * <p>
     * Note: This method automatically attaches an internal {@link ListenerAdapter} to the
     * JDA instance to handle command execution.
     * </p>
     *
     * @param jda         The active JDA instance for command registration.
     * @param reflections The pre-scanned reflection metadata.
     * @throws IllegalStateException If the registrar is already initialized or validation fails.
     */
    @Override
    public void register(JDA jda, Reflections reflections) {
        if (listenerRegistered) {
            throw new IllegalStateException("Methods for this scanner instance can only be registered once");
        }

        logger.debug("Starting global scan for SlashCommands");

        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(SlashCommand.class);
        List<SlashCommandData> commandDataList = new ArrayList<>();

        annotatedMethods.forEach(method -> registerCommand(method, commandDataList));

        commands = commandDataList;
        jda.addEventListener(listener);
        listenerRegistered = true;
    }

    /**
     * Performs a clean shutdown of the registrar.
     * <p>
     * This removes the command listener from the JDA instance and clears all internal
     * reflection caches (methods, instances) to prevent memory leaks and ensure
     * a safe state for potential re-initialization.
     * </p>
     *
     * @param jda The JDA instance to remove the listener from.
     */
    @Override
    public void shutdown(JDA jda){
        logger.info("shutting down SlashcommandRegistrar");

        if(jda!= null){
            jda.removeEventListener(listener);
            logger.debug("SlashCommandListener removed successfully");
        }
        commandMethods.clear();
        commandInstances.clear();
        instanceCache.clear();
        listenerRegistered = false;
        logger.info("SlashCommandRegistrar stopped successfully");
    }

    /**
     * Processes a single method to extract command data and register it internally.
     *
     * @param method          The reflected method annotated with {@link SlashCommand}.
     * @param commandDataList The list where the built {@link SlashCommandData} will be added.
     */
    private void registerCommand(Method method, List<SlashCommandData> commandDataList) {

        CommandParserUtil.validateMethod(method);

        SlashCommand annotation = method.getAnnotation(SlashCommand.class);
        String name = annotation.command().toLowerCase();
        String description = annotation.description();
        Class<?> declaringClass = method.getDeclaringClass();
        Option[] options = annotation.options();

        try {
            Object instance = instanceCache.get(declaringClass);

            if (instance == null) {
                instance = declaringClass.getDeclaredConstructor().newInstance();
                instanceCache.put(declaringClass, instance);
            }

            commandMethods.put(name, method);
            commandInstances.put(name, instance);

            SlashCommandData commandData = Commands.slash(name, description);

            commandData.addOptions(CommandParserUtil.parseOptions(options));

            commandDataList.add(commandData);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create command class for " + method.getName(), e);
        }
    }

    @Override
    public List<SlashCommandData> getCommands(){
        return commands;
    }

    /**
     * Internal listener that intercepts Discord interactions and invokes
     * the mapped reflection methods of this scanner instance.
     */
    private class SlashCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String name = event.getName();

            if (commandMethods.containsKey(name)) {
                try {
                    Method method = commandMethods.get(name);
                    Object instance = commandInstances.get(name);

                    method.invoke(instance, event);
                } catch (Exception e) {
                    logger.error("Error executing command: {}", name, e);
                }
            }
        }
    }
}