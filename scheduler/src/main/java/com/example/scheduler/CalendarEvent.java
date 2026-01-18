package com.example.scheduler;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.UUID;

/**
 * Class that represents a Calendar event - an event with a fixed start and end time to place in the schedule.
 * The event may be a task (scheduled event) or a fixed Event (imported from ICS)
 */
@Entity
public class CalendarEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private Boolean fixedEvent = false;  // Keep as Boolean for JPA

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String title;
    private int workMinutes;

    public CalendarEvent() {}

    public CalendarEvent(LocalDate date, LocalTime startTime, LocalTime endTime, String title) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.workMinutes = Math.abs(Math.toIntExact(Duration.between(startTime, endTime).toMinutes()));
        this.fixedEvent = false;  // Initialize here
    }

    public CalendarEvent(LocalDate date, LocalTime startTime, LocalTime endTime, String title, int workMinutes) {
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.title = title;
        this.workMinutes = workMinutes;
        this.fixedEvent = false;  // Initialize here
    }

    // Getters and Setters
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getWorkMinutes() { return workMinutes; }
    public void setWorkMinutes(Integer workMinutes) { this.workMinutes = workMinutes; }

    public UUID getId() { return this.id; }

    // ALIAS METHODS for Thymeleaf templates
    public LocalTime getStart() { return startTime; }
    public LocalTime getEnd() { return endTime; }

    public void setAsFixedEvent() {
        this.fixedEvent = true;
    }
    public void setFixedEvent(Boolean fixedEvent) {  // Standard setter
        this.fixedEvent = fixedEvent;
    }
    public boolean isFixedEvent() {
        return fixedEvent != null && fixedEvent;
    }
}