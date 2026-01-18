//package com.example.scheduler;
//
//import com.example.scheduler.ScheduleLogic.TimeSlot;
//import com.example.scheduler.ScheduleLogic.ScheduleEntry;
//import com.example.scheduler.ScheduleLogic.CalendarEvent;
//
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Schedule page controller - communicates with HTML to display the weekly schedule
// * Uses ScheduleLogic to handle the actual scheduling algorithm
// */
//public class Schedule {
//    private ScheduleLogic scheduleLogic;
//    private SlotGenerationService slotGenerationService;
//
//    public Schedule() {
//        this.scheduleLogic = new ScheduleLogic();
//        this.slotGenerationService = new SlotGenerationService();
//    }
//
//    /**
//     * Generate complete schedule for display on the schedule page
//     * This is the main method called from your HTML/controller
//     */
//    public List<ScheduleEntry> generateSchedule(UserProfile userProfile,
//                                                List<Task> tasks,
//                                                List<CalendarEvent> blockedEvents,
//                                                LocalDate startDate,
//                                                int days) {
//        List<ScheduleEntry> allEntries = new ArrayList<>();
//
//        // Step 1: Generate available time slots
//        List<TimeSlot> availableSlots = slotGenerationService.generateSlots(
//                userProfile, startDate, days, blockedEvents
//        );
//
//        // Step 2: Get incomplete tasks and sort by priority
//        List<Task> incompleteTasks = tasks.stream()
//                .filter(t -> !t.isDone())
//                .toList();
//
//        List<Task> sortedTasks = sortTasksByPriority(incompleteTasks, userProfile, startDate);
//
//        // Step 3: Assign each task to available slots using ScheduleLogic
//        int slotIndex = 0;
//
//        for (Task task : sortedTasks) {
//            int remainingMinutes = task.getEstimatedMinutes();
//
//            while (remainingMinutes > 0 && slotIndex < availableSlots.size()) {
//                TimeSlot slot = availableSlots.get(slotIndex);
//
//                // Calculate how much time is in this slot
//                long slotMinutes = Duration.between(slot.getStart(), slot.getEnd()).toMinutes();
//
//                // Determine how much work fits in this slot
//                int minutesToSchedule = (int) Math.min(remainingMinutes, slotMinutes);
//
//                // Use ScheduleLogic to create entries with breaks
//                List<ScheduleEntry> taskEntries = scheduleLogic.scheduleTaskWithBreaks(
//                        slot.getDate(),
//                        slot.getStart(),
//                        minutesToSchedule,
//                        UUID.fromString(String.valueOf(task.getId())),
//                        task.getName()
//                );
//
//                allEntries.addAll(taskEntries);
//                remainingMinutes -= minutesToSchedule;
//
//                // Move to next slot if this one is fully used
//                if (minutesToSchedule >= slotMinutes) {
//                    slotIndex++;
//                }
//            }
//
//            // Warning if task couldn't be fully scheduled
//            if (remainingMinutes > 0) {
//                System.out.println("Warning: Could not fully schedule task '" +
//                        task.getName() + "'. " + remainingMinutes +
//                        " minutes remaining.");
//            }
//        }
//
//        return allEntries;
//    }
//
//    /**
//     * Sort tasks by priority score, with tie-breaker
//     */
//    private List<Task> sortTasksByPriority(List<Task> tasks, UserProfile userProfile, LocalDate today) {
//        List<Task> sorted = new ArrayList<>(tasks);
//
//        sorted.sort((t1, t2) -> {
//            double p1 = t1.computePriority(today);
//            double p2 = t2.computePriority(today);
//            int comparison = Double.compare(p2, p1); // Higher priority first
//
//            if (comparison == 0) {
//                comparison = applyTieBreaker(t1, t2, userProfile.getTieBreakerRule());
//            }
//            return comparison;
//        });
//
//        return sorted;
//    }
//
//    /**
//     * Apply tie-breaker when tasks have equal priority
//     */
//    private int applyTieBreaker(Task t1, Task t2, UserProfile.TieBreakerRule rule) {
//        return switch (rule) {
//            case EARLIEST_DUE -> {
//                if (t1.getDueDateTime() == null) yield 1;
//                if (t2.getDueDateTime() == null) yield -1;
//                yield t1.getDueDateTime().compareTo(t2.getDueDateTime());
//            }
//            case HIGHEST_IMPORTANCE -> Integer.compare(t2.getImportance(), t1.getImportance());
//            case SHORTEST_TASK -> Integer.compare(t1.getEstimatedMinutes(), t2.getEstimatedMinutes());
//            case LONGEST_TASK -> Integer.compare(t2.getEstimatedMinutes(), t1.getEstimatedMinutes());
//        };
//    }
//
//    /**
//     * Get schedule entries for a specific date (for daily view)
//     */
//    public List<ScheduleEntry> getScheduleForDate(List<ScheduleEntry> allEntries, LocalDate date) {
//        return allEntries.stream()
//                .filter(e -> e.getDate().equals(date))
//                .toList();
//    }
//
//    /**
//     * Get schedule entries for a date range (for weekly view)
//     */
//    public List<ScheduleEntry> getScheduleForRange(List<ScheduleEntry> allEntries,
//                                                   LocalDate startDate,
//                                                   LocalDate endDate) {
//        return allEntries.stream()
//                .filter(e -> !e.getDate().isBefore(startDate) && !e.getDate().isAfter(endDate))
//                .toList();
//    }
//
//    /**
//     * Get schedule grouped by date (useful for calendar-style display)
//     */
//    public java.util.Map<LocalDate, List<ScheduleEntry>> getScheduleByDate(List<ScheduleEntry> allEntries) {
//        java.util.Map<LocalDate, List<ScheduleEntry>> grouped = new java.util.TreeMap<>();
//
//        for (ScheduleEntry entry : allEntries) {
//            grouped.computeIfAbsent(entry.getDate(), k -> new ArrayList<>()).add(entry);
//        }
//
//        return grouped;
//    }
//
//    /**
//     * Format schedule entry for display
//     */
//    public String formatEntry(ScheduleEntry entry) {
//        return String.format("%s: %s - %s (%d min) - %s",
//                entry.getDate(),
//                entry.getStart(),
//                entry.getEnd(),
//                entry.getWorkMinutes(),
//                entry.getTitle()
//        );
//    }
//}