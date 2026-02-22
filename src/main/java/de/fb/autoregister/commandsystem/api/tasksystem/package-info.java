/**
 * Provides classes and annotations for the automated background task system.
 * <p>
 * Unlike Slash Commands, which are reactive to user input, tasks in this package
 * are proactive and run based on a time-defined schedule. The
 * {@link de.fb.autoregister.commandsystem.api.tasksystem.Task} annotation is used
 * to mark logic that should be handled by the internal scheduler.
 * </p>
 *
 * @author Florian Becker
 */
package de.fb.autoregister.commandsystem.api.tasksystem;