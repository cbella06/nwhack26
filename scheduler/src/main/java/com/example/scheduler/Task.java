package com.example.scheduler;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    private String name;

    private LocalDateTime dueDateTime;

    private int importance;

    private int estimatedMinutes;

    private int complexity;

    @ElementCollection
    @CollectionTable(name = "task_tags", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "tag")
    private Set<String> tags;

    private boolean done = false;

    public Task() {
        // JPA requires default constructor
    }

    public Task(String name,
                LocalDateTime dueDateTime,
                int importance,
                int estimatedMinutes,
                int complexity,
                Set<String> tags) {
        this.name = name;
        this.dueDateTime = dueDateTime;
        this.importance = importance;
        this.estimatedMinutes = estimatedMinutes;
        this.complexity = complexity;
        this.tags = tags;
        this.done = false;
    }

    public double computePriority(LocalDate today) {
        double I = this.importance;

        double D;
        if (this.dueDateTime != null) {
            D = java.time.temporal.ChronoUnit.DAYS.between(today, this.dueDateTime.toLocalDate());
            if (D < 0) D = 0;
        } else {
            D = 7;
        }

        double T = this.estimatedMinutes / 60.0;
        double C = this.complexity;

        return (3 * I) + (10.0 / (D + 1)) + T + C;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    //public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getDueDateTime() { return dueDateTime; }
    public void setDueDateTime(LocalDateTime dueDateTime) { this.dueDateTime = dueDateTime; }

    public int getImportance() { return importance; }
    public void setImportance(int importance) { this.importance = importance; }

    public int getEstimatedMinutes() { return estimatedMinutes; }
    public void setEstimatedMinutes(int estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }

    public int getComplexity() { return complexity; }
    public void setComplexity(int complexity) { this.complexity = complexity; }

    public Set<String> getTags() { return tags; }
    public void setTags(Set<String> tags) { this.tags = tags; }

    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}