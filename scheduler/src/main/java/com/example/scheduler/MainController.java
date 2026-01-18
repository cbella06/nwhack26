package com.example.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
public class MainController {

    @Autowired
    private TaskRepository taskRepository;
    private ScheduleLogic scheduleLogic;

    // home page, displaying the tasks
    @GetMapping("/")
    public String index(Model model) {


        return "index";
    };

    @GetMapping("/tasks")
    public String tasks(Model model){
        Iterable<Task> tasks = taskRepository.findAll();
        // attribute name used in HTML
        model.addAttribute("tasks", tasks);
        return "tasks";
    }
    @GetMapping("/profile")
    public String profile() {
        return "profile";
    };

    @GetMapping("/login")
    public String login() {
        return "login";
    };

    @PostMapping("/tasks/add")
    public String addNewTask(Task task) {
        taskRepository.save(task);
        return "redirect:/";
    }

}