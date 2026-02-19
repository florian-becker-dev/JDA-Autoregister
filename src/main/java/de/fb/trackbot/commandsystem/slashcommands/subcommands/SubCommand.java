package de.fb.trackbot.commandsystem.slashcommands.subcommands;

import de.fb.trackbot.commandsystem.slashcommands.Option;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a Subcommand within a {@link SlashCommandGroup}.
 * <p>
 * Methods annotated with {@code @SubCommand} are automatically discovered by the
 * {@link SubCommandRegistrar} if their declaring class is marked with a
 * {@link SlashCommandGroup} annotation.
 * </p>
 * <b>Method Requirements:</b>
 * <ul>
 * <li>The method must be {@code public}.</li>
 * <li>The method must return {@code void}.</li>
 * <li>The method must accept exactly one parameter: {@code SlashCommandInteractionEvent}.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {
    /**
     * The name of the subcommand.
     * <p>
     * Note: Discord requires names to be lowercase and match the regex {@code ^[\w-]{1,32}$}.
     * </p>
     * @return The subcommand name.
     */
    String name();

    /**
     * A brief description of what the subcommand does.
     * <p>
     * Displayed as a tooltip in the Discord client. Limit: 100 characters.
     * </p>
     * @return The subcommand description.
     */
    String description();

    /**
     * Optional parameters for the subcommand.
     * <p>
     * Use the {@link Option} annotation to define arguments that users can
     * provide when executing this command.
     * </p>
     * @return An array of options, defaults to empty.
     */
    Option[] options() default {};
}