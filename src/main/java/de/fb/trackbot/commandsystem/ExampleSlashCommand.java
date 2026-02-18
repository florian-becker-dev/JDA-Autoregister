package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import java.util.Objects;

/**
 * Example class demonstrating the automated Slash Command system.
 * <p>
 * This class serves as a template for developers to implement new commands.
 * Methods annotated with {@link SlashCommand} are automatically discovered
 * and registered by the {@link SlashCommandScanner}.
 * </p>
 */
public class ExampleSlashCommand {

    /**
     * Simplest command form with no parameters.
     * <p>Discord usage: {@code /ping}</p>
     *
     * @param event The interaction event provided by JDA.
     */
    @SlashCommand(command = "ping", description = "returns pong")
    public void pingCommand(SlashCommandInteractionEvent event) {
        event.reply("pong").queue();
    }

    /**
     * Command with a mandatory string parameter.
     * <p>Discord usage: {@code /say text:<value>}</p>
     *
     * @param event The interaction event containing the "text" option.
     */
    @SlashCommand(command = "say", description = "says the provided String",
            options = {
                    @Option(name = "text", description = "The message to be repeated", optionType = OptionType.STRING)
            })
    public void sayCommand(SlashCommandInteractionEvent event) {
        String content = Objects.requireNonNull(event.getOption("text")).getAsString();
        event.reply(content).queue();
    }

    /**
     * Complex command with predefined choices.
     * <p>
     * Users are presented with a fixed selection list instead of free-text input.
     * Discord usage: {@code /color color:<selection>}
     * </p>
     *
     * @param event The interaction event containing the selected color choice.
     */
    @SlashCommand(command = "color", description = "returns your favorite color",
            options = {
                    @Option(name = "color", description = "pick your favorite color", optionType = OptionType.STRING,
                            choices = {
                                    @Choice(name = "Green", value = "green"),
                                    @Choice(name = "Yellow", value = "yellow"),
                                    @Choice(name = "Red", value = "red"),
                                    @Choice(name = "Black", value = "black")
                            })}
    )
    public void favoriteColorCommand(SlashCommandInteractionEvent event) {
        String color = Objects.requireNonNull(event.getOption("color")).getAsString();
        event.reply("Your favorite color is " + color).queue();
    }
}