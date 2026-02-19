package de.fb.trackbot.commandsystem;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;
import org.reflections.Reflections;

import java.util.concurrent.ExecutorService;

/**
 * Common interface for all command and logic registration modules.
 * <p>
 * A {@code FeatureRegistrar} acts as a bridge between the central {@link AnnotationScanner}
 * and specific Discord functionalities (e.g., Slash Commands, Subcommands, Tasks).
 * It is responsible for parsing relevant metadata from the classpath and managing
 * the lifecycle of these components.
 * </p>
 * * <b>Implementation Note:</b> All implementations should ensure thread-safety for internal
 * caches and handle proper cleanup within the {@link #shutdown(JDA)} method.
 */
public interface FeatureRegistrar {

    /**
     * Initializes the feature and registers it within the JDA ecosystem.
     * <p>
     * This method is typically called once during the bot's startup sequence.
     * Implementations should use the provided {@link Reflections} object to find
     * specific annotations and map them to executable logic.
     * </p>
     *  @param jda         The active JDA instance used for Discord API interaction.
     * @param reflections The pre-initialized reflection metadata containing the scanned classpath.
     */
    void register(JDA jda, Reflections reflections);

    /**
     * Gracefully shuts down the feature and releases all occupied resources.
     * <p>
     * This method must be used to:
     * <ul>
     * <li>Remove attached {@link EventListener EventListeners}.</li>
     * <li>Stop running background threads or {@link ExecutorService Executors}.</li>
     * <li>Clear internal reflection or instance caches to aid Garbage Collection.</li>
     * </ul>
     * </p>
     *  @param jda The JDA instance to detach listeners from.
     */
    void shutdown(JDA jda);
}