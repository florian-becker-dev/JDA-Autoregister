package de.fb.autoregister.commandsystem.api.tasksystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.DayOfWeek;

/**
 * Marks a method as a scheduled background task.
 * <p>
 * Methods annotated with {@code @Task} are automatically discovered by the
 * {@code TaskRegistrar} and scheduled based on the defined time and weekdays.
 * This is ideal for recurring maintenance, status updates, or reporting.
 * </p>
 * <b>Requirements:</b>
 * <ul>
 * <li>The method must return {@code void}.</li>
 * <li>The method must have no parameters.</li>
 * <li>The declaring class must have a public no-args constructor.</li>
 * </ul>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task {
    /**
     * Defines on which days of the week the task should be executed.
     * @return An array of {@link DayOfWeek}, defaults to all days (Monday-Sunday).
     */
    DayOfWeek[] weekdays() default {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };

    /**
     * The hour of the day when the task should run (0-23).
     * @return The hour in 24-hour format.
     */
    int hour();

    /**
     * The minute of the hour when the task should run (0-59).
     * @return The minute, defaults to 0.
     */
    int minutes() default 0;
}