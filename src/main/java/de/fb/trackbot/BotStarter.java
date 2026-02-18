package de.fb.trackbot;

import de.fb.trackbot.commandsystem.Choice;
import de.fb.trackbot.commandsystem.Option;
import de.fb.trackbot.commandsystem.SlashCommand;
import de.fb.trackbot.commandsystem.SlashCommandScanner;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;
import java.util.Objects;

public class BotStarter {

    private static final String TOKEN = "";

    public static void main(String[] args) {
        JDA jda = JDABuilder.createLight(TOKEN, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .build();

    }

    @SlashCommand(
            command = "ping",
            description = "returns pong",
            options = {
                    @Option(name = "olaf", description = "OLF", optionType = OptionType.STRING,
                            choices = {
                                @Choice(name = "Name", value = "Value"),
                                @Choice(name = "Rüdiger", value = "2")
                        })
            })
    public void nio(SlashCommandInteractionEvent event){
        String a = Objects.requireNonNull(event.getOption("olaf")).getAsString();
    }

}
