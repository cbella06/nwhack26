package com.example.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
public class MainController {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ScheduleLogic scheduleLogic;

    // home page, displaying the tasks
    @GetMapping("/")
    public String index(Model model) {

//        Iterable<Task> tasks = taskRepository.findAll();
//
////        List<TaskTest> tasks = Arrays.asList(
////            new TaskTest("nwHacks Pitch", "2026-01-17 10:30:00"),
////            new TaskTest("Lunch with Team", "2026-01-17 12:30:00"),
////            new TaskTest("Code Review", "2026-01-17 15:00:00")
////        );
//
//        // attribute name used in HTML
//        model.addAttribute("tasks", tasks);

        return "index";
    };

    @GetMapping("/tasks")
    public String tasks(Model model){
        Iterable<Task> tasks = taskRepository.findAll();
        // attribute name used in HTML
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

//    @GetMapping("/profile")
//    public String profile() {
//        return "profile";
//    };

    @GetMapping("/login")
    public String login() {
        return "login";
    };

    @PostMapping("/tasks/add")
    public String addNewTask(Task task) {
        taskRepository.save(task);
        return "redirect:/";
    }

//    @GetMapping("/schedule") public String schedule(Model model) { List<ScheduleLogic.CalendarEvent> events = List.of();
//        // empty list
//        List<ScheduleLogic.ScheduleEntry> entries = scheduleLogic.buildWeeklySchedule(
//                LocalDate.now(),
//                LocalTime.now(),
//                LocalTime.now().plusHours(24*7),
//                events );
//        model.addAttribute("entries", entries);
//        model.addAttribute("weekStart", LocalDate.now());
//
//        return "schedule"; }
@GetMapping("/schedule")
public String schedule(Model model) {
    List<ScheduleLogic.CalendarEvent> events = List.of(); // empty list

    LocalDate weekStart = LocalDate.now();
    LocalTime workStart = LocalTime.of(9, 0);
    LocalTime workEnd   = LocalTime.of(23, 0);

    List<ScheduleLogic.ScheduleEntry> entries =
            scheduleLogic.buildWeeklySchedule(weekStart, workStart, workEnd, events);

    model.addAttribute("entries", entries);
    model.addAttribute("weekStart", weekStart);
    return "schedule";
}

}