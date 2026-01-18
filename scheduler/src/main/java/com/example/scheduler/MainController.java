package com.example.scheduler;

import com.example.scheduler.database.CalendarEventRepository;
import com.example.scheduler.database.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
public class MainController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private CalendarEventRepository calendarEventRepository;
    @Autowired private ScheduleLogic scheduleLogic;
    @Autowired private TaskManager taskManager;

    @GetMapping("/")
    public String index(Model model) {
        LocalDate today = LocalDate.now();

        // For now: hardcode the window (later replace with profile values)
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd   = LocalTime.of(23, 0);

        // TODO: convert DB calendar events into ScheduleLogic.CalendarEvent if you want blocking
        List<CalendarEvent> blockedEvents = List.of();

        List<ScheduleLogic.ScheduleEntry> todayEntries =
                scheduleLogic.buildDailySchedule(today, workStart, workEnd, List.of(), blockedEvents);

        model.addAttribute("todayEntries", todayEntries);

        // Optional: if you still want to show raw calendar events somewhere on index.html
        model.addAttribute("events", calendarEventRepository.findAll());

        return "index";
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        model.addAttribute("tasks", taskRepository.findAll());
        return "tasks";
    }

    @PostMapping("/tasks/clear")
    public String clearTasks() {
        taskManager.clearAllTasks();
        return "redirect:/tasks";
    }

    @PostMapping("/tasks/add")
    public String addNewTask(Task task) {
        taskRepository.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {
        LocalDate weekStart = LocalDate.now();
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd   = LocalTime.of(23, 0);

        List<CalendarEvent> blockedEvents = List.of();

        List<ScheduleLogic.ScheduleEntry> entries =
                scheduleLogic.buildWeeklySchedule(weekStart, workStart, workEnd, blockedEvents);

        model.addAttribute("entries", entries);
        model.addAttribute("weekStart", weekStart);
        return "schedule";
    }
}
