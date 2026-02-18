package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Automated scanner for Discord Slash Commands using Reflection.
 * <p>
 * This class identifies methods annotated with {@link SlashCommand} within the project
 * and automatically registers them to the Discord API via JDA. It also handles
 * the routing of incoming interaction events to their respective methods.
 * </p>
 * <b>Requirements:</b>
 * <ul>
 * <li>Methods must be annotated with {@code @SlashCommand}</li>
 * <li>Methods must return {@code void}</li>
 * <li>Methods must accept exactly one parameter of type {@link SlashCommandInteractionEvent}</li>
 * <li>The declaring class must have a public no-args constructor</li>
 * </ul>
 */
public class SlashCommandScanner {

    private final Logger logger = LoggerFactory.getLogger(SlashCommandScanner.class);

    /** Map storing command names linked to their executable Methods. */
    private final Map<String, Method> commandMethods = new HashMap<>();

    /** Map storing command names linked to the instance of the class containing the method. */
    private final Map<String, Object> commandInstances = new HashMap<>();

    /** Internal cache to ensure each class is only instantiated once. */
    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    /** Ensures that the listener for this scanner instance only gets registered once. */
    private boolean listenerRegistered = false;

    /**
     * Scans the project for methods annotated with {@link SlashCommand}.
     * Discovered commands are built and sent to Discord globally.
     *
     * @param jda The JDA instance used to register the commands and the listener.
     * @throws IllegalStateException If an annotated method violates requirements or is called multiple times.
     */
    public void registerMethods(JDA jda) {
        if (listenerRegistered) {
            throw new IllegalStateException("Methods for this scanner instance can only be registered once");
        }

        logger.debug("Starting global scan for SlashCommands");

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("")
                .addScanners(Scanners.MethodsAnnotated)
        );

        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(SlashCommand.class);
        List<SlashCommandData> commandDataList = new ArrayList<>();

        annotatedMethods.forEach(method -> registerCommand(method, commandDataList));

        jda.updateCommands().addCommands(commandDataList).queue(
                success -> logger.info("{} Commands successfully registered", commandMethods.size()),
                error -> logger.error("JDA update failed", error)
        );

        jda.addEventListener(new SlashCommandListener());
        listenerRegistered = true;
    }

    /**
     * Processes a single method to extract command data and register it internally.
     *
     * @param method          The reflected method annotated with {@link SlashCommand}.
     * @param commandDataList The list where the built {@link SlashCommandData} will be added.
     */
    private void registerCommand(Method method, List<SlashCommandData> commandDataList) {
        validateMethodDeclaration(method);

        SlashCommand annotation = method.getAnnotation(SlashCommand.class);
        String name = annotation.command();
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

            addOptionsToCommand(commandData, options);

            commandDataList.add(commandData);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to create command class for " + method.getName(), e);
        }
    }

    /**
     * Configures the command options and their respective choices for a Slash Command.
     * <p>
     * This method iterates through the {@link Option} annotations, converts them into
     * JDA {@link OptionData}, and attaches any defined {@link Choice} values.
     * </p>
     * @param commandData The {@link SlashCommandData} instance to which the options are added.
     * @param options     An array of {@link Option} annotations containing the option metadata.
     * @throws IllegalArgumentException If an option type does not support choices but choices are provided.
     */
    private void addOptionsToCommand(SlashCommandData commandData, Option[] options) {
        for (Option option : options) {
            OptionData optionData =
                    new OptionData(
                            option.optionType(),
                            option.name(),
                            option.description(),
                            option.required()
                    );

            for (Choice choice : option.choices()) {
               ChoiceParser.parseChoice(option.optionType(),choice,optionData);
            }

            commandData.addOptions(optionData);
        }
    }

    /**
     * Validates that the annotated method meets all requirements for a Slash Command.
     */
    private void validateMethodDeclaration(Method method) {
        if (!method.getReturnType().equals(void.class))
            throw new IllegalStateException("Method " + method.getName() + " must return void");

        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || !params[0].equals(SlashCommandInteractionEvent.class))
            throw new IllegalStateException("Method " + method.getName() + " must have exactly one parameter of type SlashCommandInteractionEvent");
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