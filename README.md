# JDA-Autoregister

A modern, annotation-driven framework for **JDA (Java Discord API)** that automates the registration of commands and recurring tasks. Built for **Java 21**.

---

## Features

* **Automatic Registration**: Eliminates the need for manual `.addCommands()` calls in your main class.
* **Slash Commands**: Define commands directly at the method level with full support for options and choices.
* **Subcommand Groups**: Organize related logic within classes while maintaining object state across interactions.
* **Task System**: Schedule recurring background tasks using the `@Task` annotation.
* **Zero-Config**: Includes an optional automatic package detection system for seamless setup.

---

##  Installation

Add the following to the `pom.xml` of your **Bot Project**:

### 1. Add the Repository
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
### 2. Add the Dependency 
```xml
<dependency>
	    <groupId>com.github.florian-becker-dev</groupId>
	    <artifactId>JDA-Autoregister</artifactId>
	    <version>v1.0.1</version>
	</dependency>
```
---
## Usage 
### 1. Initializing the Bot
Simply register the `BotListener` when building your JDA instance. It will automatically trigger the annotation scan as soon as the bot reaches the `READY` state.
```java
public static void main(String[] args) {
    JDABuilder.createDefault("YOUR_TOKEN")
        .addEventListeners(new BotListener()) 
        .build();
}
```
### 2. Slash Commands
You can define commands in any class. Here is the simplest form compared to a complex one with options:

#### A Simple Command (/ping)
```java
public class MyCommands {
    @SlashCommand(command = "ping", description = "returns pong")
    public void pingCommand(SlashCommandInteractionEvent event) {
        event.reply("pong").queue();
    }
}
```
#### A Complex Command with Options (/color)

```java
public class ExampleSlashCommand {
    @SlashCommand(command = "color", description = "returns your favorite color",
            options = {
                    @Option(name = "color", description = "pick your favorite color", optionType = OptionType.STRING,
                            choices = {
                                    @Choice(name = "Green", value = "green"),
                                    @Choice(name = "Red", value = "red")
                            })}
    )
    public void favoriteColorCommand(SlashCommandInteractionEvent event) {
        String color = Objects.requireNonNull(event.getOption("color")).getAsString();
        event.reply("Your favorite color is " + color).queue();
    }
}
```

### 3. Subcommand Groups 
Group related functionality under a single root command. The framework caches the class instance, allowing you to persist data between different command executions. 

```java
@SlashCommandGroup(name = "config", description = "provides configuration commands")
public class SubCommandsExample {
    private int version = 0;

    @SubCommand(name = "get", description = "returns the current Version")
    public void handleGet(SlashCommandInteractionEvent event){
        event.reply("Current version is: " + version).queue();
    }

    @SubCommand(name = "set", description = "updates the version to specified Integer", options = {
            @Option(name = "version", description = "new version", optionType = OptionType.INTEGER, required = true)
    })
    public void handleSet(SlashCommandInteractionEvent event){
        version = Objects.requireNonNull(event.getOption("version")).getAsInt();
        event.reply("Version successfully changed to: " + version).setEphemeral(true).queue();
    }
}
```

### 4. Background Tasks 
Automate recurring tasks by annotating a Method 

```java
public class TestTask {
    @Task(hour = 20) // Executes every day at 20:00
    public void test(){
        System.out.println("Daily task executed successfully!");
    }
}
```
---

## Java Module System (Jigsaw)
As the libary utilizes 21 modules your bot's `module-info.java` must open your command packages to `org.reflections` and this library to allow for runtime annotation scanning: 

```java
module your.bot.module {
    requires de.fb.jdaautoregister;
    
    // Replace with the package containing your commands
    opens your.bot.commands to de.fb.jdaautoregister, org.reflections;
}
```

---
## Licence 
Distributed under the Apache License, Version 2.0. See the `LICENSE` file for more information.  

