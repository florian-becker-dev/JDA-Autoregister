package de.fb.trackbot.commandsystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Represents a predefined choice for a command option.
 * <p>
 * When choices are provided, users can only select from the list
 * instead of typing free-form input.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Choice {

    /**
     * The name shown to the user in the Discord selection menu.
     * @return The display name of the choice.
     */
    String name();

    /**
     * The internal value sent to the bot when this choice is selected.
     * <p>
     * Although stored as a String in this annotation, the {@code ChoiceParser}
     * will convert it to a numeric type if the parent {@link Option}
     * uses {@link net.dv8tion.jda.api.interactions.commands.OptionType#INTEGER} or
     * {@link net.dv8tion.jda.api.interactions.commands.OptionType#NUMBER}.
     * </p>
     * @return The choice value.
     */
    String value();
}