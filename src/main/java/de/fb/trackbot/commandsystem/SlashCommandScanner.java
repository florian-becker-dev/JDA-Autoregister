package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Method;
import java.util.*;

public class SlashCommandScanner {

    private static final Map<String, Method> commandMethods = new HashMap<>();
    private static final Map<String, Object> commandInstances = new HashMap<>();

    public static void registerMethods(JDA jda) {

        Reflections reflections = new Reflections("de.fb.trackbot", Scanners.MethodsAnnotated);
        Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(SlashCommand.class);

        List<SlashCommandData> commandDataList = new ArrayList<>();

        annotatedMethods.forEach(method -> {

            if(!method.getReturnType().equals(void.class))
                throw new IllegalStateException(method.getName() + "must return void");

            Class<?> [] params = method.getParameterTypes();
            if(params.length != 1 || !params[0].equals(SlashCommandInteractionEvent.class))
                throw new IllegalStateException(method.getName() + "must have exactly one parameter of type SlashCommandInteractionEvent");

            SlashCommand annotation = method.getAnnotation(SlashCommand.class);
            String name = annotation.command();
            String description = annotation.description();

            try {
                Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
                commandMethods.put(name, method);
                commandInstances.put(name, instance);

                SlashCommandData commandData = Commands.slash(name, description);

                // Parameter 1
                if (!annotation.parameter1().isEmpty()) {
                    commandData.addOption(
                            OptionType.STRING,
                            annotation.parameter1(),
                            annotation.parameter1Description(),
                            annotation.parameter1Required()
                    );
                }

                // Parameter 2
                if (!annotation.parameter2().isEmpty()) {
                    commandData.addOption(
                            OptionType.STRING,
                            annotation.parameter2(),
                            annotation.parameter2Description(),
                            annotation.parameter2Required()
                    );
                }

                commandDataList.add(commandData);

            } catch (Exception e) {
                throw new RuntimeException("Invalid Constructor call");
            }
        });

        jda.updateCommands().addCommands(commandDataList).queue();

        jda.addEventListener(new SlashCommandListener());
    }

    public static class SlashCommandListener extends ListenerAdapter {
        @Override
        public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
            String name = event.getName();
            if (commandMethods.containsKey(name)) {
                try {
                    Method method = commandMethods.get(name);
                    Object instance = commandInstances.get(name);

                    method.invoke(instance, event);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

