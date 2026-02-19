package de.fb.trackbot.commandsystem.slashcommands;

import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChoiceParser {

    private static final Logger logger = LoggerFactory.getLogger(ChoiceParser.class);

    /**
     * Parses and adds a choice to the option data based on the supported Discord types.
     */
    public static void parseChoice(OptionType type, Choice choice, OptionData optionData) {
        try {
            switch (type) {
                case INTEGER -> optionData.addChoice(choice.name(), getAsLong(choice.value()));
                case NUMBER -> optionData.addChoice(choice.name(), getAsDouble(choice.value()));
                case STRING -> optionData.addChoice(choice.name(), choice.value());
                default -> logger.error("Cannot add choices for OptionType {}. Only STRING, INTEGER, and NUMBER are supported.", type);
            }
        } catch (NumberFormatException e) {
            logger.error("Failed to parse choice value '{}' for command option '{}' as {}. Check your annotations!",
                    choice.value(), optionData.getName(), type);
        }
    }

    private static long getAsLong(String value) {
        return Long.parseLong(value);
    }

    private static double getAsDouble(String value) {
        return Double.parseDouble(value);
    }
}