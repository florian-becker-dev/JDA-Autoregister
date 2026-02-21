package de.fb.autoregister.commandsystem;

import de.fb.autoregister.commandsystem.api.slashcommands.Choice;
import de.fb.autoregister.commandsystem.slashcommands.ChoiceParser;
import de.fb.autoregister.commandsystem.api.slashcommands.Option;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared utility for validating command methods and parsing option annotations.
 * This centralizes logic to ensure consistency across different registrar types.
 */
public class CommandParserUtil {

    /**
     * Validates that a method meets the framework's requirements for interaction handling.
     * @param method The method to validate.
     * @throws IllegalStateException if validation fails.
     */
    public static void validateMethod(Method method) {
        if (!method.getReturnType().equals(void.class)) {
            throw new IllegalStateException("Method " + method.getName() + " in "
                    + method.getDeclaringClass().getSimpleName() + " must return void");
        }

        Class<?>[] params = method.getParameterTypes();
        if (params.length != 1 || !params[0].equals(SlashCommandInteractionEvent.class)) {
            throw new IllegalStateException("Method " + method.getName() + " in "
                    + method.getDeclaringClass().getSimpleName() + " must accept exactly one SlashCommandInteractionEvent");
        }
    }

    /**
     * Converts an array of {@link Option} annotations into a list of JDA {@link OptionData}.
     * @param options The option annotations to parse.
     * @return A list of ready-to-use OptionData.
     */
    public static List<OptionData> parseOptions(Option[] options) {
        List<OptionData> optionDataList = new ArrayList<>();

        for (Option option : options) {
            OptionData data = new OptionData(
                    option.optionType(),
                    option.name(),
                    option.description(),
                    option.required()
            );

            for (Choice choice : option.choices()) {
                ChoiceParser.parseChoice(option.optionType(), choice, data);
            }

            optionDataList.add(data);
        }
        return optionDataList;
    }
}