package com.example.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskTest {
    private String name;
    private int id;
    private LocalDateTime dueDateTime;
    private Boolean done;
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//    private int importance;
//    private int estimatedMinutes;
//    private int complexity;
//    private Set<String> tags;

    /**
     * Creates a basic task.
     * @param name name of the task
     * @param dueTime date the task is due in HH:mm format
     */
    TaskTest(String name, String dueTime) {
        this.name = "Test Task";
        this.id = 1;
        this.dueDateTime =  LocalDateTime.parse(dueTime, formatter);
        this.done = false;
    }

    public String getName() { return name; }
    public LocalDateTime getTime() { return dueDateTime; }
}

