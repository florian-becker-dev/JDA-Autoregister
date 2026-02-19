package de.fb.trackbot;


import de.fb.trackbot.commandsystem.AnnotationScanner;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.util.EnumSet;


public class BotStarter {

    private static final String TOKEN = "";

    public static void main(String[] args) {
        JDA jda = JDABuilder.createLight(TOKEN, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .build();

        AnnotationScanner annotationScanner = new AnnotationScanner(jda);
        annotationScanner.registerFeatures();

    }

}
