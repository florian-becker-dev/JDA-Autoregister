package de.fb.autoregister.commandsystem.api;

import de.fb.autoregister.commandsystem.AnnotationScanner;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code BotListener} serves as the primary entry point for the JDA-Autoregister framework.
 * <p>
 * This listener monitors the {@link ReadyEvent} to ensure the JDA instance is fully
 * initialized before triggering the automatic scanning and registration process.
 * </p>
 * * <b>Usage Example:</b>
 * <pre>{@code
 * JDABuilder.createDefault("YOUR_TOKEN")
 * .addEventListeners(new BotListener())
 * .build();
 * }</pre>
 * @author Florian Becker
 * @version 1.0.2
 * @see AnnotationScanner
 */
public class BotListener extends ListenerAdapter {

    /**
     * Triggered when JDA has finished loading all entities and is ready for use.
     * <p>
     * This method initializes the {@link AnnotationScanner} using the JDA instance
     * provided by the event and starts the registration of all annotated features
     * (e.g., {@code @SlashCommand}, {@code @Task}).
     * </p>
     * @param event The {@link ReadyEvent} fired by JDA upon successful startup.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        AnnotationScanner scanner = new AnnotationScanner(event.getJDA());
        scanner.registerFeatures();
    }
}