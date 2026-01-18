package com.example.scheduler;

import org.springframework.stereotype.Service;

import javax.management.loading.PrivateClassLoader;
import java.time.Duration;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ScheduleLogic {
    private static final int BLOCK_MINUTES = 15;
    private TaskManager taskManager;

    public ScheduleLogic(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

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
        for (CalendarEvent event : events) {
            if (!event.getDate().equals(date)) continue;

            //event block
            for (TimeBlock block : blocks) {
                LocalTime blockStart = block.getStart();
                LocalTime blockEnd = blockStart.plusMinutes(BLOCK_MINUTES);

                boolean overlaps =
                        blockStart.isBefore(event.getEnd()) &&
                                blockEnd.isAfter(event.getStart());

                if (overlaps) {
                    block.setBlocked(true);
                }
            }
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
            task.setEstimatedMinutes(remainingBlocks * BLOCK_MINUTES);
            if (remainingBlocks > 0) {
                System.out.println(
                        "WARNING: Task \"" + task.getName() +
                                "\" could not be fully scheduled (" +
                                remainingBlocks * BLOCK_MINUTES + " minutes left)"
                );
            }
        }
    }

    private List<ScheduleEntry> buildScheduleEntries(
            LocalDate date,
            List<TimeBlock> blocks,
            List<Task> prioritizedTasks
    ) {
        List<ScheduleEntry> entries = new ArrayList<>();

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
    private ScheduleEntry createEntry(
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

        return new ScheduleEntry(
                date,
                start,
                end,
                taskId,
                task.getName(),
                minutes
        );
    }




    public List<ScheduleEntry> buildDailySchedule(
            LocalDate date,
            LocalTime workStart,
            LocalTime workEnd,
            List<Task> prioritizedTasks,
            List<CalendarEvent> events
    ) {
        List<TimeBlock> blocks = generateDailyBlocks(workStart, workEnd);
        applyCalendarEvents(date, blocks, events);
        applyBreaks(blocks);

        List<Task> incompleteTasks = new ArrayList<>();
        incompleteTasks = taskManager.getIncompleteTasks();
        placeTasks(blocks, incompleteTasks);

        return buildScheduleEntries(date, blocks, incompleteTasks);
    }

    public List<ScheduleEntry> buildWeeklySchedule(
            LocalDate weekStart,
            LocalTime workStart,
            LocalTime workEnd,
            List<CalendarEvent> events
    ) {
        List<ScheduleEntry> weeklyEntries = new ArrayList<>();

        // Get prioritized tasks ONCE
        List<Task> tasks = taskManager.getIncompleteTasks();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);

            // Stop early if all tasks are done
            boolean allDone = tasks.stream()
                    .allMatch(t -> t.getEstimatedMinutes() <= 0);
            if (allDone) break;

            List<TimeBlock> blocks = generateDailyBlocks(workStart, workEnd);
            applyCalendarEvents(date, blocks, events);
            applyBreaks(blocks);
            placeTasks(blocks, tasks);

            weeklyEntries.addAll(
                    buildScheduleEntries(date, blocks, tasks)
            );
        }

        return weeklyEntries;
    }



    // ===== ScheduleEntry =====
    static class ScheduleEntry {
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
    static class CalendarEvent {
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
