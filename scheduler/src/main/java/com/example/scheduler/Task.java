package com.example.scheduler;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
public class Task {

    @Id
    private UUID id; // generated elsewhere (store/service)
//    private int userId;
    private String name;
    // Allow null → treated as end of week by scheduler

    @DateTimeFormat
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

    public double computePriority(LocalDate today) {
        // I = Importance (1-5)
        double I = this.importance;

        // D = Days until due
        double D;
        if (this.dueDateTime != null) {
            D = java.time.temporal.ChronoUnit.DAYS.between(today, this.dueDateTime.toLocalDate());
            // If overdue, treat as 0 days (maximum urgency)
            if (D < 0) D = 0;
        } else {
            // No due date = treat as far in future (low urgency)
            D = 7;
        }

        // T = Estimated time in hours
        double T = this.estimatedMinutes / 60.0;

        // C = Complexity (1-5)
        double C = this.complexity;

        // Priority = (3*I) + 10/(D+1) + T + C
        return (3 * I) + (10.0 / (D + 1)) + T + C;
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
