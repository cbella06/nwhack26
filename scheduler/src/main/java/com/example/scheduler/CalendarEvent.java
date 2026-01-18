package com.example.scheduler;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;
import java.util.UUID;

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

    // Fixed event methods - CHANGED to return Boolean not boolean
    public void setAsFixedEvent() {
        this.fixedEvent = true;
    }

    public Boolean getFixedEvent() {  // Standard getter name
        return fixedEvent != null ? fixedEvent : false;
    }

    public void setFixedEvent(Boolean fixedEvent) {  // Standard setter
        this.fixedEvent = fixedEvent;
    }

    // Alternative: keep isFixedEvent but make sure it handles null
    public boolean isFixedEvent() {
        return fixedEvent != null && fixedEvent;
    }
}