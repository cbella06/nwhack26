package com.example.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * TaskManager - Communicates with the task management HTML page
 * Handles all task CRUD operations and provides data for display
 */
public class TaskManager {
    private List<Task> tasks;

    public TaskManager() {
        this.tasks = new ArrayList<>();
    }

    // ===== HTML Form Submission Handlers =====

    /**
     * Handle new task form submission from HTML
     * Called when user submits the "Add Task" form
     */
    public Task createTaskFromForm(String name,
                                   String dueDateTimeStr,  // From HTML datetime-local input
                                   int importance,
                                   int estimatedMinutes,
                                   int complexity,
                                   Set<String> tags) {
        LocalDateTime dueDateTime = null;
        if (dueDateTimeStr != null && !dueDateTimeStr.isEmpty()) {
            dueDateTime = LocalDateTime.parse(dueDateTimeStr);
        }

        Task task = new Task(name, dueDateTime, importance, estimatedMinutes, complexity, tags);
        tasks.add(task);
        return task;
    }

    /**
     * Handle edit task form submission from HTML
     * Called when user updates an existing task
     */
    public boolean updateTaskFromForm(String taskIdStr,
                                      String name,
                                      String dueDateTimeStr,
                                      int importance,
                                      int estimatedMinutes,
                                      int complexity,
                                      Set<String> tags) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = getTaskById(taskId);

            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setName(name);

                if (dueDateTimeStr != null && !dueDateTimeStr.isEmpty()) {
                    task.setDueDateTime(LocalDateTime.parse(dueDateTimeStr));
                } else {
                    task.setDueDateTime(null);
                }

                task.setImportance(importance);
                task.setEstimatedMinutes(estimatedMinutes);
                task.setComplexity(complexity);
                task.setTags(tags);
                return true;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
        }
        return false;
    }

    /**
     * Handle task completion checkbox toggle from HTML
     */
    public boolean toggleTaskCompletion(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = getTaskById(taskId);

            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setDone(!task.isDone());  // Toggle
                return true;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
        }
        return false;
    }

    /**
     * Handle task deletion from HTML
     * Called when user clicks delete button
     */
    public boolean deleteTaskFromHTML(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            return removeTask(taskId);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
            return false;
        }
    }

    // ===== Data Retrieval for HTML Display =====

    /**
     * Get all tasks formatted for HTML table/list display
     */
    public List<TaskDisplayData> getTasksForDisplay() {
        return tasks.stream()
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    /**
     * Get incomplete tasks for the "To Do" view
     */
    public List<TaskDisplayData> getIncompleteTasksForDisplay() {
        return tasks.stream()
                .filter(t -> !t.isDone())
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    /**
     * Get completed tasks for the "Completed" view
     */
    public List<TaskDisplayData> getCompletedTasksForDisplay() {
        return tasks.stream()
                .filter(Task::isDone)
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks grouped by tag for filtered view
     */
    public java.util.Map<String, List<TaskDisplayData>> getTasksGroupedByTag() {
        java.util.Map<String, List<TaskDisplayData>> grouped = new java.util.HashMap<>();

        for (Task task : tasks) {
            if (task.getTags() != null) {
                for (String tag : task.getTags()) {
                    grouped.computeIfAbsent(tag, k -> new ArrayList<>())
                            .add(convertToDisplayData(task));
                }
            }
        }

        return grouped;
    }

    /**
     * Get task by ID for editing form pre-population
     */
    public TaskDisplayData getTaskForEdit(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = getTaskById(taskId);
            return taskOpt.map(this::convertToDisplayData).orElse(null);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
            return null;
        }
    }

    /**
     * Get summary statistics for dashboard display
     */
    public TaskSummary getTaskSummary() {
        int total = tasks.size();
        int incomplete = (int) tasks.stream().filter(t -> !t.isDone()).count();
        int completed = (int) tasks.stream().filter(Task::isDone).count();
        int overdue = (int) tasks.stream()
                .filter(t -> !t.isDone() &&
                        t.getDueDateTime() != null &&
                        t.getDueDateTime().isBefore(LocalDateTime.now()))
                .count();

        return new TaskSummary(total, incomplete, completed, overdue);
    }

    // ===== Core CRUD Operations =====

    /**
     * Add a new task
     */
    public Task addTask(String name,
                        LocalDateTime dueDateTime,
                        int importance,
                        int estimatedMinutes,
                        int complexity,
                        Set<String> tags) {
        Task task = new Task(name, dueDateTime, importance, estimatedMinutes, complexity, tags);
        tasks.add(task);
        return task;
    }

    /**
     * Add an existing task object
     */
    public void addTask(Task task) {
        if (task != null) {
            tasks.add(task);
        }
    }

    /**
     * Get all tasks (for Schedule generation)
     */
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks);
    }

    /**
     * Get a specific task by ID
     */
    public Optional<Task> getTaskById(UUID taskId) {
        return tasks.stream()
                .filter(t -> t.getId().equals(taskId))
                .findFirst();
    }

    /**
     * Get all incomplete tasks sorted by priority (highest to lowest)
     */
    public List<Task> getIncompleteTasks(UserProfile userProfile) {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(t -> !t.isDone())
                .sorted((t1, t2) -> {
                    double p1 = t1.computePriority(today);
                    double p2 = t2.computePriority(today);
                    int comparison = Double.compare(p2, p1); // Higher priority first

                    // Apply tie-breaker if priorities are equal
                    if (comparison == 0) {
                        comparison = applyTieBreaker(t1, t2, userProfile.getTieBreakerRule());
                    }
                    return comparison;
                })
                .collect(Collectors.toList());
    }

    /**
     * Apply tie-breaker when tasks have equal priority
     */
    private int applyTieBreaker(Task t1, Task t2, UserProfile.TieBreakerRule rule) {
        return switch (rule) {
            case EARLIEST_DUE -> {
                if (t1.getDueDateTime() == null) yield 1;
                if (t2.getDueDateTime() == null) yield -1;
                yield t1.getDueDateTime().compareTo(t2.getDueDateTime());
            }
            case HIGHEST_IMPORTANCE -> Integer.compare(t2.getImportance(), t1.getImportance());
            case SHORTEST_TASK -> Integer.compare(t1.getEstimatedMinutes(), t2.getEstimatedMinutes());
            case LONGEST_TASK -> Integer.compare(t2.getEstimatedMinutes(), t1.getEstimatedMinutes());
        };
    }/**
     * Get all incomplete tasks sorted by priority (highest to lowest)
     */
    public List<Task> getIncompleteTasks() {
        LocalDate today = LocalDate.now();
        return tasks.stream()
                .filter(t -> !t.isDone())
                .sorted((t1, t2) -> {
                    double p1 = t1.computePriority(today);
                    double p2 = t2.computePriority(today);
                    return Double.compare(p2, p1); // Higher priority first
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all completed tasks
     */
    public List<Task> getCompletedTasks() {
        return tasks.stream()
                .filter(Task::isDone)
                .collect(Collectors.toList());
    }

    /**
     * Get tasks by tag
     */
    public List<Task> getTasksByTag(String tag) {
        return tasks.stream()
                .filter(t -> t.getTags() != null && t.getTags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * Get overdue tasks
     */
    public List<Task> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.stream()
                .filter(t -> !t.isDone() &&
                        t.getDueDateTime() != null &&
                        t.getDueDateTime().isBefore(now))
                .collect(Collectors.toList());
    }

    /**
     * Get tasks sorted by priority
     */
    public List<Task> getTasksSortedByPriority() {
        LocalDate today = LocalDate.now();
        List<Task> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort((t1, t2) -> {
            double p1 = t1.computePriority(today);
            double p2 = t2.computePriority(today);
            return Double.compare(p2, p1);
        });
        return sortedTasks;
    }

    /**
     * Mark a task as complete
     */
    public boolean markTaskComplete(UUID taskId) {
        Optional<Task> taskOpt = getTaskById(taskId);
        if (taskOpt.isPresent()) {
            taskOpt.get().setDone(true);
            return true;
        }
        return false;
    }

    /**
     * Mark a task as incomplete
     */
    public boolean markTaskIncomplete(UUID taskId) {
        Optional<Task> taskOpt = getTaskById(taskId);
        if (taskOpt.isPresent()) {
            taskOpt.get().setDone(false);
            return true;
        }
        return false;
    }

    /**
     * Remove a task by ID
     */
    public boolean removeTask(UUID taskId) {
        return tasks.removeIf(t -> t.getId().equals(taskId));
    }

    /**
     * Remove all completed tasks
     */
    public int removeCompletedTasks() {
        int initialSize = tasks.size();
        tasks.removeIf(Task::isDone);
        return initialSize - tasks.size();
    }

    /**
     * Clear all tasks
     */
    public void clearAllTasks() {
        tasks.clear();
    }

    /**
     * Get all unique tags
     */
    public Set<String> getAllTags() {
        return tasks.stream()
                .filter(t -> t.getTags() != null)
                .flatMap(t -> t.getTags().stream())
                .collect(Collectors.toSet());
    }

    // ===== Helper Methods =====

    /**
     * Convert Task to display-friendly format for HTML
     */
    private TaskDisplayData convertToDisplayData(Task task) {
        return new TaskDisplayData(
                task.getId().toString(),
                task.getName(),
                task.getDueDateTime() != null ? task.getDueDateTime().toString() : "",
                task.getImportance(),
                task.getEstimatedMinutes(),
                task.getComplexity(),
                task.getTags() != null ? String.join(", ", task.getTags()) : "",
                task.isDone(),
                task.computePriority(LocalDate.now())
        );
    }

    // ===== Inner Classes for HTML Data Transfer =====

    /**
     * Data class for displaying tasks in HTML
     */
    public static class TaskDisplayData {
        public final String id;
        public final String name;
        public final String dueDateTime;
        public final int importance;
        public final int estimatedMinutes;
        public final int complexity;
        public final String tags;
        public final boolean done;
        public final double priority;

        public TaskDisplayData(String id, String name, String dueDateTime,
                               int importance, int estimatedMinutes, int complexity,
                               String tags, boolean done, double priority) {
            this.id = id;
            this.name = name;
            this.dueDateTime = dueDateTime;
            this.importance = importance;
            this.estimatedMinutes = estimatedMinutes;
            this.complexity = complexity;
            this.tags = tags;
            this.done = done;
            this.priority = priority;
        }

        // Getters for JSON serialization or template engines
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDueDateTime() { return dueDateTime; }
        public int getImportance() { return importance; }
        public int getEstimatedMinutes() { return estimatedMinutes; }
        public int getComplexity() { return complexity; }
        public String getTags() { return tags; }
        public boolean isDone() { return done; }
        public double getPriority() { return priority; }
    }

    /**
     * Summary statistics for dashboard
     */
    public static class TaskSummary {
        public final int total;
        public final int incomplete;
        public final int completed;
        public final int overdue;

        public TaskSummary(int total, int incomplete, int completed, int overdue) {
            this.total = total;
            this.incomplete = incomplete;
            this.completed = completed;
            this.overdue = overdue;
        }

        public int getTotal() { return total; }
        public int getIncomplete() { return incomplete; }
        public int getCompleted() { return completed; }
        public int getOverdue() { return overdue; }
    }
}