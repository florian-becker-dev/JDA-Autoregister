package de.fb.trackbot.commandsystem.slashcommands.subcommands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a container for Discord Subcommands.
 * <p>
 * Classes annotated with {@code @SlashCommandGroup} are recognized by the
 * {@link SubCommandRegistrar} as root commands. All methods within this class
 * annotated with {@link SubCommand} will be registered as children of this group.
 * </p>
 * <b>Requirements:</b>
 * <ul>
 * <li>The annotated class must have a public no-args constructor for instantiation.</li>
 * <li>Discord allows only one level of nesting (Command -> Subcommand) in this
 * specific framework implementation.</li>
 * </ul>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlashCommandGroup {
    /**
     * The name of the root command group.
     * <p>
     * This represents the primary command a user types (e.g., {@code /settings}).
     * Must be lowercase and match Discord's naming requirements.
     * </p>
     * @return The group name.
     */
    String name();

    /**
     * The description of the command group.
     * <p>
     * Briefly explains the purpose of this command category in the Discord UI.
     * </p>
     * @return The group description.
     */
    String description();
}