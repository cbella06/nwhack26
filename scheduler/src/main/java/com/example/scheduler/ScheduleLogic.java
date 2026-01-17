package com.example.scheduler;

import java.time.LocalTime;
import java.time.LocalDate;
import java.util.UUID;

public class ScheduleLogic {
    class WorkingWindow {
        private LocalTime start;
        private LocalTime end;

        public WorkingWindow() {}

        public WorkingWindow(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        // Getters and Setters
        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }
    }

    // ===== TimeSlot =====
    class TimeSlot {
//        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
        private Double productivity;

        public TimeSlot() {}

        public TimeSlot(LocalTime start, LocalTime end, Double productivity) {
//            this.date = date;
            this.start = start;
            this.end = end;
            this.productivity = productivity;
        }

        // Getters and Setters
//        public LocalDate getDate() { return date; }
//        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }

        public Double getProductivity() { return productivity; }
        public void setProductivity(Double productivity) { this.productivity = productivity; }
    }

    // ===== ScheduleEntry =====
    class ScheduleEntry {
        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
        private UUID id;
        private String title;
        private Integer workMinutes;

        public ScheduleEntry() {}

        public ScheduleEntry(LocalDate date, LocalTime start, LocalTime end, UUID id,
                             String title, Integer workMinutes) {
            this.date = date;
            this.start = start;
            this.end = end;
            this.id = id;
            this.title = title;
            this.workMinutes = workMinutes;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }

        public UUID getId() { return id; }
        public void setId(UUID id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public Integer getWorkMinutes() { return workMinutes; }
        public void setWorkMinutes(Integer workMinutes) { this.workMinutes = workMinutes; }
    }

    // ===== CalendarEvent (for blocked times) =====
    class CalendarEvent {
        private LocalDate date;
        private LocalTime start;
        private LocalTime end;
        private String title;

        public CalendarEvent() {}

        public CalendarEvent(LocalDate date, LocalTime start, LocalTime end, String title) {
            this.date = date;
            this.start = start;
            this.end = end;
            this.title = title;
        }

        // Getters and Setters
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
    }

}
