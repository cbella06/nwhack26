package com.example.scheduler;

import com.example.scheduler.database.CalendarEventRepository;
import com.example.scheduler.database.TaskRepository;
import com.example.scheduler.database.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;


@Controller
public class MainController {

    @Autowired private TaskRepository taskRepository;
    @Autowired private CalendarEventRepository calendarEventRepository;
    @Autowired private ScheduleLogic scheduleLogic;
    @Autowired private UserProfileRepository userProfileRepository;


    @GetMapping("/")
    public String index(Model model) {
        LocalDate today = LocalDate.now();

        UserProfile profile = userProfileRepository
                .findByUsername("default")
                .orElse(null);

        // fallback if profile or window doesn't exist
        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(17, 0);

        if (profile != null && !profile.getWorkingWindows().isEmpty()) {
            WorkingWindow w = profile.getWorkingWindows().get(0);
            workStart = w.getStartTime();
            workEnd = w.getEndTime();
        }

        // Finds all blocked events
        List<CalendarEvent> blockedEvents =
                calendarEventRepository.findAll();
        // Gets today's entries
        List<CalendarEvent> todayEntries =
                scheduleLogic.buildDailySchedule(today, workStart, workEnd, List.of(), blockedEvents);
        // Add fixed events to today's entries
        todayEntries.addAll(calendarEventRepository.findByDate(LocalDate.now()));
        todayEntries.sort(Comparator.comparing(CalendarEvent::getStartTime));

        model.addAttribute("todayEntries", todayEntries);
        model.addAttribute("events", calendarEventRepository.findAll());
        return "index";
    }


    @GetMapping("/tasks")
    public String tasks(Model model) {
        model.addAttribute("tasks", taskRepository.findAll());
        return "tasks";
    }

    @PostMapping("/tasks/add")
    public String addNewTask(Task task) {
        taskRepository.save(task);
        return "redirect:/tasks";
    }

    @GetMapping("/schedule")
    public String schedule(Model model) {

        LocalDate weekStart = LocalDate.now();

        UserProfile profile = userProfileRepository
                .findByUsername("default")
                .orElse(null);

        LocalTime workStart = LocalTime.of(9, 0);
        LocalTime workEnd = LocalTime.of(17, 0);

        if (profile != null && !profile.getWorkingWindows().isEmpty()) {
            WorkingWindow w = profile.getWorkingWindows().get(0);
            workStart = w.getStartTime();
            workEnd = w.getEndTime();
        }

        List<CalendarEvent> blockedEvents =
                calendarEventRepository.findAll();

        List<CalendarEvent> entries =
                scheduleLogic.buildWeeklySchedule(weekStart, workStart, workEnd, blockedEvents);
        entries.addAll(calendarEventRepository.findAll());

        Map<LocalDate, List<CalendarEvent>> entriesByDate =
                entries.stream()
                        .collect(Collectors.groupingBy(
                               CalendarEvent::getDate,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        for (LocalDate day : entriesByDate.keySet()) {
            entriesByDate.get(day).sort(Comparator.comparing(CalendarEvent::getStartTime));
        }
        model.addAttribute("entriesByDate", entriesByDate);
        model.addAttribute("weekStart", weekStart);

        return "schedule";
    }

}
