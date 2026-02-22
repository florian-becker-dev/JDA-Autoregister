/**
 * Provides the annotations required to define complex Slash Command hierarchies.
 * <p>
 * This package contains annotations for structuring commands into
 * {@link de.fb.autoregister.commandsystem.api.slashcommands.subcommands.SlashCommandGroup Groups}
 * and {@link de.fb.autoregister.commandsystem.api.slashcommands.subcommands.SubCommand SubCommands},
 * following the official Discord API specifications for command nesting.
 * </p>
 * * <b>Hierarchical Structure:</b>
 * <ul>
 * <li>Main Command (defined via {@code @SlashCommand})</li>
 * <li>Optional: Subcommand Group (defined via {@code @SlashCommandGroup})</li>
 * <li>Executable: Subcommand (defined via {@code @SubCommand})</li>
 * </ul>
 * @author Florian Becker
 *
 */
package de.fb.autoregister.commandsystem.api.slashcommands.subcommands;