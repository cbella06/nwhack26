package com.example.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Generates daily and weekly schedules based on tasks to complete and preexisting commitments.
 */
@Service
public class ScheduleLogic {
    private static final int BLOCK_MINUTES = 15;

    @Autowired
    private TaskManager taskManager;

    class TimeBlock {
        private LocalTime start;
        private boolean blocked;      // calendar events
        private boolean breakBlock;
        private UUID taskId;          // null if free

        public TimeBlock(LocalTime start) {
            this.start = start;
            this.blocked = false;
            this.breakBlock = false;
            this.taskId = null;
        }

        public LocalTime getStart() { return start; }
        public boolean isBlocked() { return blocked; }
        public void setBlocked(boolean blocked) { this.blocked = blocked; }

        public boolean isBreakBlock() { return breakBlock; }
        public void setBreakBlock(boolean breakBlock) { this.breakBlock = breakBlock; }

        public UUID getTaskId() { return taskId; }
        public void setTaskId(UUID taskId) { this.taskId = taskId; }
    }

    // generate the list of 15 min blocks of the day
    private List<TimeBlock> generateDailyBlocks(
            LocalTime workStart,
            LocalTime workEnd
    ) {
        List<TimeBlock> blocks = new ArrayList<>();

        LocalTime time = workStart;
        while (time.isBefore(workEnd)) {
            blocks.add(new TimeBlock(time));
            time = time.plusMinutes(BLOCK_MINUTES);
        }

        return blocks;
    }

    private void applyCalendarEvents(
            LocalDate date,
            List<TimeBlock> blocks,
            List<CalendarEvent> events
    ) {
        System.out.println("DEBUG: Applying calendar events for " + date);
        System.out.println("DEBUG: Total events to check: " + events.size());

        for (CalendarEvent event : events) {
            System.out.println("DEBUG: Checking event: " + event.getTitle() +
                    " on " + event.getDate() +
                    " from " + event.getStartTime() +
                    " to " + event.getEndTime());

            if (!event.getDate().equals(date)) {
                System.out.println("DEBUG: Event date doesn't match, skipping");
                continue;
            }

            System.out.println("DEBUG: Event matches date! Blocking time blocks...");
            int blockedCount = 0;

            //event block
            for (TimeBlock block : blocks) {
                LocalTime blockStart = block.getStart();
                LocalTime blockEnd = blockStart.plusMinutes(BLOCK_MINUTES);

                boolean overlaps =
                        blockStart.isBefore(event.getEndTime()) &&
                                blockEnd.isAfter(event.getStartTime());

                if (overlaps) {
                    block.setBlocked(true);
                    blockedCount++;
                }
            }
            System.out.println("DEBUG: Blocked " + blockedCount + " time blocks for this event");
        }
    }

    private void applyBreaks(List<TimeBlock> blocks) {
        int count = 0;
        for (TimeBlock block: blocks){
            if(block.isBlocked()) {
                count = 0;
                continue;
            }
            count++;
            if(count==4){
                block.setBlocked(true);
                block.setBreakBlock(true);
                count=0;
            }
        }
    }

    private void placeTasks(List<TimeBlock> blocks, List<Task> tasks){
        for (Task task : tasks) {

            int remainingBlocks = task.getEstimatedMinutes() / BLOCK_MINUTES;

            for (TimeBlock block : blocks) {
                if (remainingBlocks == 0) break;
                if (block.isBlocked()) continue;
                if (block.getTaskId() != null) continue;

                block.setTaskId(task.getId());
                remainingBlocks--;
            }
            if (remainingBlocks > 0) {
                System.out.println(
                        "WARNING: Task \"" + task.getName() +
                                "\" could not be fully scheduled (" +
                                remainingBlocks * BLOCK_MINUTES + " minutes left)"
                );
            }
        }
    }

    private List<CalendarEvent> buildScheduleEntries(
            LocalDate date,
            List<TimeBlock> blocks,
            List<Task> prioritizedTasks
    ) {
        List<CalendarEvent> entries = new ArrayList<>();

        TimeBlock currentStart = null;
        UUID currentTaskId = null;

        for (TimeBlock block : blocks) {

            if (block.getTaskId() == null) {
                if (currentStart != null) {
                    entries.add(createEntry(
                            date,
                            currentStart.getStart(),
                            block.getStart(),
                            currentTaskId,
                            prioritizedTasks
                    ));
                    currentStart = null;
                    currentTaskId = null;
                }
                continue;
            }

            // New task or first task
            if (currentTaskId == null || !block.getTaskId().equals(currentTaskId)) {

                // Close previous entry
                if (currentStart != null) {
                    entries.add(createEntry(
                            date,
                            currentStart.getStart(),
                            block.getStart(),
                            currentTaskId,
                            prioritizedTasks
                    ));
                }

                currentStart = block;
                currentTaskId = block.getTaskId();
            }
        }

        // Close final entry
        if (currentStart != null) {
            TimeBlock lastBlock = blocks.get(blocks.size() - 1);
            entries.add(createEntry(
                    date,
                    currentStart.getStart(),
                    lastBlock.getStart().plusMinutes(BLOCK_MINUTES),
                    currentTaskId,
                    prioritizedTasks
            ));
        }

        return entries;
    }

    private CalendarEvent createEntry(
            LocalDate date,
            LocalTime start,
            LocalTime end,
            UUID taskId,
            List<Task> tasks
    ) {
        Task task = tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst()
                .orElseThrow();

        int minutes = (int) Duration.between(start, end).toMinutes();

        return new CalendarEvent(
                date,
                start,
                end,
                task.getName(),
                minutes
        );
    }

    public List<CalendarEvent> buildDailySchedule(
            LocalDate date,
            LocalTime workStart,
            LocalTime workEnd,
            List<Task> prioritizedTasks,
            List<CalendarEvent> events
    ) {
        List<TimeBlock> blocks = generateDailyBlocks(workStart, workEnd);
        applyCalendarEvents(date, blocks, events);
        applyBreaks(blocks);

        List<Task> incompleteTasks = taskManager.getIncompleteTasks();
        placeTasks(blocks, incompleteTasks);

        return buildScheduleEntries(date, blocks, incompleteTasks);
    }

    public List<CalendarEvent> buildWeeklySchedule(LocalDate weekStart,
                                                   LocalTime workStart,
                                                   LocalTime workEnd,
                                                   List<CalendarEvent> blockedEvents) {

        List<CalendarEvent> allEntries = new ArrayList<>();

        // fetch prioritized tasks ONCE for the whole week
        List<Task> tasks = taskManager.getIncompleteTasks();

        // track remaining work per task in 15-min blocks
        java.util.Map<UUID, Integer> remainingBlocks = new java.util.LinkedHashMap<>();
        for (Task t : tasks) {
            remainingBlocks.put(t.getId(), Math.max(0, t.getEstimatedMinutes() / BLOCK_MINUTES));
        }

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            List<TimeBlock> blocks = generateDailyBlocks(workStart, workEnd);
            applyCalendarEvents(date, blocks, blockedEvents);
            applyBreaks(blocks);

            // place tasks using the remaining map (spill across days)
            placeTasksWithRemaining(blocks, tasks, remainingBlocks);

            allEntries.addAll(buildScheduleEntries(date, blocks, tasks));
        }

        return allEntries;
    }


    private void placeTasksWithRemaining(List<TimeBlock> blocks,
                                         List<Task> tasks,
                                         java.util.Map<UUID, Integer> remainingBlocks) {

        for (Task task : tasks) {
            int left = remainingBlocks.getOrDefault(task.getId(), 0);
            if (left <= 0) continue;

            for (TimeBlock block : blocks) {
                if (left == 0) break;
                if (block.isBlocked()) continue;
                if (block.getTaskId() != null) continue;

                block.setTaskId(task.getId());
                left--;
            }

            remainingBlocks.put(task.getId(), left);
        }
    }

}