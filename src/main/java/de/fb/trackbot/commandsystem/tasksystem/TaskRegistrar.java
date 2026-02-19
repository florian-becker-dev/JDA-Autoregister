package de.fb.trackbot.commandsystem.tasksystem;

import de.fb.trackbot.commandsystem.FeatureRegistrar;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Automated registry for background tasks scheduled via annotations.
 * <p>
 * This class implements {@link FeatureRegistrar} to discover methods annotated with
 * {@link Task} via reflection. It calculates the necessary delays and manages a
 * {@link ScheduledExecutorService} to execute these tasks at their specified times.
 * </p>
 * <b>Requirements:</b>
 * <ul>
 * <li>Methods must be annotated with {@link Task}.</li>
 * <li>Methods must be {@code public}, return {@code void}, and have no parameters.</li>
 * <li>The declaring class must provide a public no-args constructor.</li>
 * </ul>
 * <b>Lifecycle Management:</b>
 * Use {@link #register(JDA, Reflections)} to start the scheduler and
 * {@link #shutdown(JDA)} to gracefully terminate background threads and ensure
 * the application can exit cleanly.
 */
public class TaskRegistrar implements FeatureRegistrar {

    private final Logger logger = LoggerFactory.getLogger(TaskRegistrar.class);

    /** Thread pool for background task execution. Optimized with a small fixed pool size. */
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    /**
     * Scans for {@link Task} annotations and initializes the background scheduler.
     *
     * @param jda         The JDA instance (unused for tasks, but part of the registrar contract).
     * @param reflections The pre-scanned reflection metadata.
     */
    @Override
    public void register(JDA jda, Reflections reflections) {
        logger.debug("Starting global scan for annotated background tasks");
        Set<Method> taskMethods = reflections.getMethodsAnnotatedWith(Task.class);
        taskMethods.forEach(this::scheduleTask);
    }

    /**
     * Validates, instantiates, and schedules a specific task method.
     * <p>
     * Calculations are performed in seconds to determine the initial delay 
     * until the first execution, followed by a fixed 24-hour period check.
     * </p>
     *
     * @param method The reflected method annotated with {@link Task}.
     */
    private void scheduleTask(Method method) {
        // Validation for method signature
        if (method.getParameterCount() != 0 || !method.getReturnType().equals(void.class)) {
            logger.error("Task method {} must be void and have no parameters!", method.getName());
            return;
        }

        Task taskAnno = method.getAnnotation(Task.class);

        try {
            // Instantiate the class containing the task
            Object instance = method.getDeclaringClass().getDeclaredConstructor().newInstance();

            long initialDelay = calculateInitialDelay(taskAnno);
            long period = TimeUnit.DAYS.toSeconds(1); // Standard 24h cycle

            scheduler.scheduleAtFixedRate(() -> {
                // Verification of allowed weekdays before execution
                if (Arrays.asList(taskAnno.weekdays()).contains(LocalDateTime.now().getDayOfWeek())) {
                    try {
                        method.invoke(instance);
                        logger.debug("Executed background task: {}", method.getName());
                    } catch (Exception e) {
                        logger.error("Error during execution of task: {}", method.getName(), e);
                    }
                }
            }, initialDelay, period, TimeUnit.SECONDS);

            logger.info("Scheduled Task '{}' for {}:{} (Initial delay: {} min)",
                    method.getName(), taskAnno.hour(), taskAnno.minutes(), initialDelay / 60);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize Task class for " + method.getName(), e);
        }
    }

    /**
     * Calculates the duration in seconds from the current time to the next 
     * occurrence of the specified task time.
     *
     * @param task The task annotation containing hour and minute metadata.
     * @return Seconds until the first execution.
     */
    private long calculateInitialDelay(Task task) {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime target = now.withHour(task.hour()).withMinute(task.minutes()).withSecond(0).withNano(0);

        // If time has already passed today, shift to tomorrow
        if (now.isAfter(target)) {
            target = target.plusDays(1);
        }

        return Duration.between(now, target).getSeconds();
    }

    /**
     * Shuts down the internal scheduler and waits for active tasks to finish.
     * <p>
     * Implements a tiered shutdown (shutdown -> wait -> shutdownNow) to prevent 
     * orphaned background threads.
     * </p>
     *
     * @param jda The JDA instance.
     */
    @Override
    public void shutdown(JDA jda) {
        logger.info("Shutting down TaskRegistrar and stopping all scheduled tasks");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("TaskRegistrar stopped successfully");
    }

    @Override
    public List<SlashCommandData> getCommands() {
        return FeatureRegistrar.super.getCommands();
    }
}