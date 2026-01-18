package com.example.scheduler;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

import java.util.Arrays;
import java.util.List;

@Controller
public class MainController {

    // home page, displaying the tasks
    @GetMapping("/")
    public String index(Model model) {

        List<TaskTest> tasks = Arrays.asList(
            new TaskTest("nwHacks Pitch", "2026-01-17 10:30:00"),
            new TaskTest("Lunch with Team", "2026-01-17 12:30:00"),
            new TaskTest("Code Review", "2026-01-17 15:00:00")
        );

        // attribute name used in HTML
        model.addAttribute("tasks", tasks);

        return "index";
    };

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    };

    @GetMapping("/login")
    public String login() {
        return "login";
    };
}