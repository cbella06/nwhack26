package com.example.scheduler;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class Task {

    private UUID id; // generated elsewhere (store/service)
    private int userId;
    private String name;
    // Allow null → treated as end of week by scheduler
    private LocalDateTime dueDateTime;
    // 1–5 (5 = most important)
    private int importance;
    // total estimated work time (in minutes)
    private int estimatedMinutes;
    // 1–5 (5 = most complex)
    private int complexity;
    private Set<String> tags;
    // default false
    private boolean done = false;

    public Task() {
        // default constructor (needed for JSON deserialization)
        this.id = UUID.randomUUID();
        this.done = false;
    }

    public Task(String name,
                LocalDateTime dueDateTime,
                int importance,
                int estimatedMinutes,
                int complexity,
                Set<String> tags) {

        this.id = UUID.randomUUID();
        this.name = name;
        this.dueDateTime = dueDateTime;
        this.importance = importance;
        this.estimatedMinutes = estimatedMinutes;
        this.complexity = complexity;
        this.tags = tags;
        this.done = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDueDateTime() {
        return dueDateTime;
    }

    public void setDueDateTime(LocalDateTime dueDateTime) {
        this.dueDateTime = dueDateTime;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public int getComplexity() {
        return complexity;
    }

    public void setComplexity(int complexity) {
        this.complexity = complexity;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
