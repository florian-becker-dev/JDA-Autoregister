package de.fb.trackbot.commandsystem.slashcommands.subcommands;

import de.fb.trackbot.commandsystem.slashcommands.Option;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Objects;

/**
 * Example implementation of a Subcommand Group using the custom Annotation System.
 * <p>
 * This class demonstrates how to group related functionalities under a single
 * root command ({@code /config}). The {@link SubCommandRegistrar} will instantiate
 * this class once and map the methods to {@code /config get} and {@code /config set}.
 * </p>
 */
@SlashCommandGroup(name = "config", description = "provides configuration commands")
public class SubCommandsExample {

    /** * Shared state within the group instance.
     * Since the registrar caches the instance, this variable persists across
     * different interactions within this group.
     */
    private int version = 0;

    /**
     * Handles the {@code /config get} command.
     * @param event The Discord interaction event.
     */
    @SubCommand(name = "get", description = "returns the current Version")
    public void handleGet(SlashCommandInteractionEvent event){
        event.reply("Current version is: " + version).queue();
    }

    /**
     * Handles the {@code /config set} command with a required option.
     * <p>
     * Demonstrates the use of the {@link Option} annotation to define
     * command parameters that are automatically parsed and registered.
     * </p>
     * @param event The Discord interaction event.
     */
    @SubCommand(name = "set", description = "sets the version to specified Integer", options = {
            @Option(name = "version", description = "new version", optionType = OptionType.INTEGER, required = true)
    })
    public void handleSet(SlashCommandInteractionEvent event){
        // Objects.requireNonNull is used as the option is marked as required in the annotation
        version = Objects.requireNonNull(event.getOption("version")).getAsInt();
        event.reply("Version successfully changed to: " + version).setEphemeral(true).queue();
    }
}