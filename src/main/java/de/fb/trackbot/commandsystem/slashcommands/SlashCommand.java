package de.fb.trackbot.commandsystem.slashcommands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be registered as a global Discord Slash Command.
 * <p>
 * Methods annotated with {@code @SlashCommand} must fulfill the following criteria:
 * <ul>
 * <li>The method must return {@code void}.</li>
 * <li>The method must have exactly one parameter of type {@link SlashCommandInteractionEvent}.</li>
 * <li>The declaring class must provide a public no-args constructor for instantiation.</li>
 * </ul>
 * </p>
 *
 * @see Option
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommand {

    /**
     * The primary name of the command (e.g., "ping").
     * <p>
     * Must be lowercase and follow Discord's naming requirements (no spaces, special characters).
     * </p>
     * @return The command name.
     */
    String command();

    /**
     * A brief description explaining what the command does.
     * <p>
     * This will be displayed in the Discord command menu.
     * </p>
     * @return The command description.
     */
    String description();

    /**
     * An optional array of command arguments/options.
     * <p>
     * Use the {@link Option} annotation to define specific parameters for this command.
     * </p>
     * @return An array of command options, defaults to an empty array.
     */
    Option[] options() default {};
}