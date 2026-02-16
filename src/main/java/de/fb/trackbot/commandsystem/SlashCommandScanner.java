package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
//TODO add logging with logging framework
/**
 * Automated scanner for Discord Slash Commands using Reflection.
 * <p>
 * This class identifies methods annotated with {@link SlashCommand} within the project
 * and automatically registers them to the Discord API via JDA. It also handles
 * the routing of incoming interaction events to their respective methods.
 * </p>
 * * <b>Requirements:</b>
 * <ul>
 * <li>Methods must be annotated with {@code @SlashCommand}</li>
 * <li>Methods must return {@code void}</li>
 * <li>Methods must accept exactly one parameter of type {@link SlashCommandInteractionEvent}</li>
 * <li>The declaring class must have a public no-args constructor</li>
 * </ul>
 * *
 */
public class SlashCommandScanner {

    private static final Logger logger = LoggerFactory.getLogger(SlashCommandScanner.class);

    /** Map storing command names linked to their executable Methods. */
    private static final Map<String, Method> commandMethods = new HashMap<>();

    /** Map storing command names linked to the instance of the class containing the method. */
    private static final Map<String, Object> commandInstances = new HashMap<>();

    /** Internal cache to ensure each class is only instantiated once. */
    private static final Map<Class<?>, Object> instanceCache = new HashMap<>();

    /** ensures that the {@link SlashCommandListener} class only gets registered once*/
    private static boolean listenerRegistered = false;

    /**
     * Scans the project for methods annotated with {@link SlashCommand}.
     * Discovered commands are built and sent to Discord globally.
     *
     * @param jda The JDA instance used to register the commands and the listener.
     * @throws IllegalStateException If an annotated method violates return type or parameter requirements.
     * @throws RuntimeException If a class instance cannot be created via reflection.
     */
    public static void registerMethods(JDA jda) {

        logger.debug("starting global scan for SlashCommands");
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage("")
                .addScanners(Scanners.MethodsAnnotated)
        );
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(SlashCommand.class);

        List<SlashCommandData> commandDataList = new ArrayList<>();

        annotatedMethods.forEach(method -> {

            // Validation: Return Type
            if(!method.getReturnType().equals(void.class))
                throw new IllegalStateException("Method " + method.getName() + " must return void");

            // Validation: Parameters
            Class<?> [] params = method.getParameterTypes();
            if(params.length != 1 || !params[0].equals(SlashCommandInteractionEvent.class))
                throw new IllegalStateException("Method " + method.getName() + " must have exactly one parameter of type SlashCommandInteractionEvent");

            SlashCommand annotation = method.getAnnotation(SlashCommand.class);
            String name = annotation.command();
            String description = annotation.description();
            Class<?> declearingClass = method.getDeclaringClass();

            try {
                Object instance = instanceCache.get(declearingClass);

                if(instance == null){
                    instance = declearingClass.getDeclaredConstructor().newInstance();
                    instanceCache.put(declearingClass, instance);
                }

                commandMethods.put(name, method);
                commandInstances.put(name, instance);

                SlashCommandData commandData = Commands.slash(name, description);

                // Option Registration Logic
                addOptionIfPresent(commandData, annotation.parameter1(), annotation.parameter1Description(), annotation.parameter1Required());
                addOptionIfPresent(commandData, annotation.parameter2(), annotation.parameter2Description(), annotation.parameter2Required());

                commandDataList.add(commandData);

            } catch (Exception e) {
               throw new IllegalStateException("Failed to create command class for " + method.getName());
            }
        });

        jda.updateCommands().addCommands(commandDataList).queue(
                success -> logger.info("{} Commands successfully registered", commandMethods.size()),
                error -> logger.error("JDA update failed")
        );

        if(!listenerRegistered){
            jda.addEventListener(new SlashCommandListener());
            listenerRegistered = true;
        }
    }

    /**
     * Helper method to reduce boilerplate when adding command options.
     */
    private static void addOptionIfPresent(SlashCommandData data, String name, String desc, boolean required) {
        if (!name.isEmpty()) {
            data.addOption(OptionType.STRING, name, desc, required);
        }
    }

    /**
     * Internal listener that intercepts Discord interactions and invokes
     * the mapped reflection methods.
     */
    public static class SlashCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String name = event.getName();

            if (commandMethods.containsKey(name)) {
                try {
                    Method method = commandMethods.get(name);
                    Object instance = commandInstances.get(name);

                    // Execute the command method
                    method.invoke(instance, event);

                } catch (Exception e) {
                    logger.error("Error executing command: {}", name, e);
                }
            }
        }
    }
}