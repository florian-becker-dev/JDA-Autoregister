package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a command option (parameter) for a Slash Command.
 * <p>
 * Options are the inputs users provide when executing a command.
 * They can range from simple strings to complex objects like users or roles.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Option {

    /**
     * The name of the option.
     * Must be lowercase and follow Discord's naming requirements (1-32 characters).
     * @return The option name.
     */
    String name();

    /**
     * A short description of what this option does.
     * Shown to the user in the Discord UI (1-100 characters).
     * @return The option description.
     */
    String description();

    /**
     * The type of the option (e.g., STRING, INTEGER, USER).
     * @return The {@link OptionType}.
     */
    OptionType optionType();

    /**
     * A fixed set of choices the user can pick from.
     * <p>
     * <b>Note:</b> Discord only supports choices for the types
     * {@link OptionType#STRING}, {@link OptionType#INTEGER}, and {@link OptionType#NUMBER}.
     * </p>
     * @return An array of {@link Choice} annotations.
     */
    Choice[] choices() default {};

    /**
     * Whether the user is required to provide a value for this option.
     * Defaults to {@code true}.
     * @return {@code true} if required, {@code false} if optional.
     */
    boolean required() default true;
}