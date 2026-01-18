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
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;


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

        List<CalendarEvent> blockedEvents = calendarEventRepository.findAll();

        List<CalendarEvent> todayEntries =
                scheduleLogic.buildDailySchedule(today, workStart, workEnd, List.of(), blockedEvents);
        todayEntries.addAll(calendarEventRepository.findByDate(LocalDate.now()));
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

        List<CalendarEvent> blockedEvents = calendarEventRepository.findAll();

        List<CalendarEvent> entries =
                scheduleLogic.buildWeeklySchedule(weekStart, workStart, workEnd, blockedEvents);

        Map<LocalDate, List<CalendarEvent>> entriesByDate =
                entries.stream()
                        .collect(Collectors.groupingBy(
                               CalendarEvent::getDate,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        model.addAttribute("entriesByDate", entriesByDate);
//        model.addAttribute("entries", entries);
        model.addAttribute("weekStart", weekStart);
        return "schedule";
    }
}
