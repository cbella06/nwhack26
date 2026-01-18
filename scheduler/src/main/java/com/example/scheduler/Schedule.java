package com.example.scheduler;

import com.example.scheduler.ScheduleLogic.ScheduleEntry;
import com.example.scheduler.ScheduleLogic.CalendarEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Schedule page controller - communicates with HTML to display the weekly schedule
 * Uses ScheduleLogic to handle the actual scheduling algorithm
 */
@Service
public class Schedule {

    @Autowired
    private ScheduleLogic scheduleLogic;

    @Autowired
    private TaskManager taskManager;

    public Schedule() {
        this.scheduleLogic = new ScheduleLogic();
    }

    /**
     * Generate complete schedule for display on the schedule page
     * For MVP: uses hardcoded work hours (can be parameterized later)
     */
    public List<ScheduleEntry> generateWeeklySchedule(
            LocalDate startDate,
            int days,
            LocalTime workStart,
            LocalTime workEnd,
            List<CalendarEvent> blockedEvents) {

        List<ScheduleEntry> allEntries = new ArrayList<>();

        // Get prioritized tasks from TaskManager
        List<Task> prioritizedTasks = taskManager.getIncompleteTasks();

        // Generate schedule for each day
        for (int i = 0; i < days; i++) {
            LocalDate currentDate = startDate.plusDays(i);

            List<ScheduleEntry> dailyEntries = scheduleLogic.buildDailySchedule(
                    currentDate,
                    workStart,
                    workEnd,
                    prioritizedTasks,
                    blockedEvents
            );

            allEntries.addAll(dailyEntries);
        }

        return allEntries;
    }

    /**
     * Generate schedule for a single day
     */
    public List<ScheduleEntry> generateDailySchedule(
            LocalDate date,
            LocalTime workStart,
            LocalTime workEnd,
            List<CalendarEvent> blockedEvents) {

        List<Task> prioritizedTasks = taskManager.getIncompleteTasks();

        return scheduleLogic.buildDailySchedule(
                date,
                workStart,
                workEnd,
                prioritizedTasks,
                blockedEvents
        );
    }

    /**
     * Generate schedule for the next 7 days with default work hours (9am-5pm)
     */
    public List<ScheduleEntry> generateDefaultWeeklySchedule(List<CalendarEvent> blockedEvents) {
        return generateWeeklySchedule(
                LocalDate.now(),
                7,
                LocalTime.of(9, 0),   // Default: 9 AM
                LocalTime.of(17, 0),  // Default: 5 PM
                blockedEvents
        );
    }

    /**
     * Get schedule entries for a specific date (for daily view)
     */
    public List<ScheduleEntry> getScheduleForDate(List<ScheduleEntry> allEntries, LocalDate date) {
        return allEntries.stream()
                .filter(e -> e.getDate().equals(date))
                .toList();
    }

    /**
     * Get schedule entries for a date range (for weekly view)
     */
    public List<ScheduleEntry> getScheduleForRange(List<ScheduleEntry> allEntries,
                                                   LocalDate startDate,
                                                   LocalDate endDate) {
        return allEntries.stream()
                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
                .toList();
    }

    /**
     * Get schedule grouped by date (useful for calendar-style display)
     */
    public Map<LocalDate, List<ScheduleEntry>> getScheduleByDate(List<ScheduleEntry> allEntries) {
        Map<LocalDate, List<ScheduleEntry>> grouped = new TreeMap<>();

        for (ScheduleEntry entry : allEntries) {
            grouped.computeIfAbsent(entry.getDate(), k -> new ArrayList<>()).add(entry);
        }

        return grouped;
    }

    /**
     * Format schedule entry for display
     */
    public String formatEntry(ScheduleEntry entry) {
        return String.format("%s: %s - %s (%d min) - %s",
                entry.getDate(),
                entry.getStart(),
                entry.getEnd(),
                entry.getWorkMinutes(),
                entry.getTitle()
        );
    }

    /**
     * Get schedule summary statistics
     */
    public ScheduleSummary getScheduleSummary(List<ScheduleEntry> entries) {
        int totalEntries = entries.size();
        int totalMinutes = entries.stream()
                .mapToInt(ScheduleEntry::getWorkMinutes)
                .sum();

        long uniqueTasks = entries.stream()
                .map(ScheduleEntry::getId)
                .distinct()
                .count();

        return new ScheduleSummary(totalEntries, totalMinutes, (int) uniqueTasks);
    }

    /**
     * Summary statistics for schedule display
     */
    public static class ScheduleSummary {
        public final int totalBlocks;
        public final int totalMinutes;
        public final int uniqueTasks;

        public ScheduleSummary(int totalBlocks, int totalMinutes, int uniqueTasks) {
            this.totalBlocks = totalBlocks;
            this.totalMinutes = totalMinutes;
            this.uniqueTasks = uniqueTasks;
        }

        public int getTotalBlocks() { return totalBlocks; }
        public int getTotalMinutes() { return totalMinutes; }
        public double getTotalHours() { return totalMinutes / 60.0; }
        public int getUniqueTasks() { return uniqueTasks; }
    }
}