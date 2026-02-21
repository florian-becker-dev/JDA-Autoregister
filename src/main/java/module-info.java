module JDA.Autoregister {
    requires net.dv8tion.jda;
    requires org.reflections;
    requires org.slf4j;
    requires static annotations;

    exports de.fb.autoregister.commandsystem.api;
    exports de.fb.autoregister.commandsystem.api.slashcommands;
    exports de.fb.autoregister.commandsystem.api.slashcommands.subcommands;
    exports de.fb.autoregister.commandsystem.api.tasksystem;

    opens de.fb.autoregister.commandsystem.api to org.reflections;
}