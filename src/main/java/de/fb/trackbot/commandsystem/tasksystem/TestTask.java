package de.fb.trackbot.commandsystem.tasksystem;

public class TestTask {

    @Task(hour =  20)
    public void test(){
        System.out.println("TESTTASK Successfull");
    }
}
