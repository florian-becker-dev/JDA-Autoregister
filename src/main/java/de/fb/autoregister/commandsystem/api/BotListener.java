package de.fb.autoregister.commandsystem.api;

import de.fb.autoregister.commandsystem.AnnotationScanner;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class BotListener extends ListenerAdapter {

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        AnnotationScanner scanner = new AnnotationScanner(event.getJDA());
        scanner.registerFeatures();
    }
}
