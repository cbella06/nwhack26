package com.example.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskManager {

    @Autowired
    private static TaskRepository taskRepository;

    // ===== HTML Form Submission Handlers =====

    public Task createTaskFromForm(String name,
                                   String dueDateTimeStr,
                                   int importance,
                                   int estimatedMinutes,
                                   int complexity,
                                   Set<String> tags) {
        LocalDateTime dueDateTime = null;
        if (dueDateTimeStr != null && !dueDateTimeStr.isEmpty()) {
            dueDateTime = LocalDateTime.parse(dueDateTimeStr);
        }

        Task task = new Task(name, dueDateTime, importance, estimatedMinutes, complexity, tags);
        return taskRepository.save(task);  // Save to database
    }

    public boolean updateTaskFromForm(String taskIdStr,
                                      String name,
                                      String dueDateTimeStr,
                                      int importance,
                                      int estimatedMinutes,
                                      int complexity,
                                      Set<String> tags) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = taskRepository.findById(taskId);

            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setName(name);

                if (dueDateTimeStr != null && !dueDateTimeStr.isEmpty()) {
                    task.setDueDateTime(dueDateTimeStr);
                }

                task.setImportance(importance);
                task.setEstimatedMinutes(estimatedMinutes);
                task.setComplexity(complexity);
                task.setTags(tags);

                taskRepository.save(task);  // Save changes to database
                return true;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
        }
        return false;
    }

    public boolean toggleTaskCompletion(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = taskRepository.findById(taskId);

            if (taskOpt.isPresent()) {
                Task task = taskOpt.get();
                task.setDone(!task.isDone());
                taskRepository.save(task);  // Save to database
                return true;
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
        }
        return false;
    }

    public boolean deleteTaskFromHTML(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            taskRepository.deleteById(taskId);  // Delete from database
            return true;
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
            return false;
        }
    }

    // ===== Data Retrieval for HTML Display =====

    public List<TaskDisplayData> getTasksForDisplay() {
        return taskRepository.findAll().stream()
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    public List<TaskDisplayData> getIncompleteTasksForDisplay() {
        return taskRepository.findByDoneFalse().stream()
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    public List<TaskDisplayData> getCompletedTasksForDisplay() {
        return taskRepository.findByDoneTrue().stream()
                .map(this::convertToDisplayData)
                .collect(Collectors.toList());
    }

    public java.util.Map<String, List<TaskDisplayData>> getTasksGroupedByTag() {
        java.util.Map<String, List<TaskDisplayData>> grouped = new java.util.HashMap<>();

        for (Task task : taskRepository.findAll()) {
            if (task.getTags() != null) {
                for (String tag : task.getTags()) {
                    grouped.computeIfAbsent(tag, k -> new ArrayList<>())
                            .add(convertToDisplayData(task));
                }
            }
        }

        return grouped;
    }

    public TaskDisplayData getTaskForEdit(String taskIdStr) {
        try {
            UUID taskId = UUID.fromString(taskIdStr);
            Optional<Task> taskOpt = taskRepository.findById(taskId);
            return taskOpt.map(this::convertToDisplayData).orElse(null);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid task ID: " + taskIdStr);
            return null;
        }
    }

    public TaskSummary getTaskSummary() {
        List<Task> allTasks = taskRepository.findAll();
        int total = allTasks.size();
        int incomplete = (int) allTasks.stream().filter(t -> !t.isDone()).count();
        int completed = (int) allTasks.stream().filter(Task::isDone).count();
        int overdue = (int) taskRepository.findByDoneFalseAndDueDateTimeBefore(LocalDateTime.now()).size();

        return new TaskSummary(total, incomplete, completed, overdue);
    }

    // ===== Core CRUD Operations =====

    public Task addTask(String name,
                        LocalDateTime dueDateTime,
                        int importance,
                        int estimatedMinutes,
                        int complexity,
                        Set<String> tags) {
        Task task = new Task(name, dueDateTime, importance, estimatedMinutes, complexity, tags);
        return taskRepository.save(task);
    }

    public void addTask(Task task) {
        if (task != null) {
            taskRepository.save(task);
        }
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Optional<Task> getTaskById(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    /**
     * Get all incomplete tasks sorted by priority (highest to lowest)
     * Uses EARLIEST_DUE as default tie-breaker for MVP
     * MAINTAINED THE SAME AS BEFORE - just gets data from database
     */
    public static List<Task> getIncompleteTasks() {
        LocalDate today = LocalDate.now();
        return taskRepository.findByDoneFalse().stream()  // Changed: get from DB instead of list
                .sorted((t1, t2) -> {
                    double p1 = t1.computePriority(today);
                    double p2 = t2.computePriority(today);
                    int comparison = Double.compare(p2, p1);

                    if (comparison == 0) {
                        if (t1.getDueDateTime() == null) return 1;
                        if (t2.getDueDateTime() == null) return -1;
                        return t1.getDueDateTime().compareTo(t2.getDueDateTime());
                    }
                    return comparison;
                })
                .collect(Collectors.toList());
    }

    public List<Task> getCompletedTasks() {
        return taskRepository.findByDoneTrue();
    }

    public List<Task> getTasksByTag(String tag) {
        return taskRepository.findByTagsContaining(tag);
    }

    public List<Task> getOverdueTasks() {
        return taskRepository.findByDoneFalseAndDueDateTimeBefore(LocalDateTime.now());
    }

    public List<Task> getTasksSortedByPriority() {
        LocalDate today = LocalDate.now();
        List<Task> sortedTasks = new ArrayList<>(taskRepository.findAll());
        sortedTasks.sort((t1, t2) -> {
            double p1 = t1.computePriority(today);
            double p2 = t2.computePriority(today);
            return Double.compare(p2, p1);
        });
        return sortedTasks;
    }

    public boolean markTaskComplete(UUID taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setDone(true);
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    public boolean markTaskIncomplete(UUID taskId) {
        Optional<Task> taskOpt = taskRepository.findById(taskId);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            task.setDone(false);
            taskRepository.save(task);
            return true;
        }
        return false;
    }

    public boolean removeTask(UUID taskId) {
        if (taskRepository.existsById(taskId)) {
            taskRepository.deleteById(taskId);
            return true;
        }
        return false;
    }

    public int removeCompletedTasks() {
        List<Task> completedTasks = taskRepository.findByDoneTrue();
        int count = completedTasks.size();
        taskRepository.deleteAll(completedTasks);
        return count;
    }

    public void clearAllTasks() {
        taskRepository.deleteAll();
    }

    public Set<String> getAllTags() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getTags() != null)
                .flatMap(t -> t.getTags().stream())
                .collect(Collectors.toSet());
    }

    // ===== Helper Methods =====

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

    // ===== Inner Classes =====

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