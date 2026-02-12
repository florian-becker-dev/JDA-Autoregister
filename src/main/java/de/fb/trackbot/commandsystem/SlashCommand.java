package de.fb.trackbot.commandsystem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SlashCommand {
    String command();
    String description();

    String parameter1() default "";
    String parameter1Description() default "";
    boolean parameter1Required() default false;

    String parameter2() default "";
    String parameter2Description() default "";
    boolean parameter2Required() default false;
}

