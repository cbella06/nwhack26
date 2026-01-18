package com.example.scheduler;

import com.example.scheduler.database.CalendarEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.List;

@Controller
public class ProfileController {

    // MVP: in-memory single profile (controller is a singleton bean)
    private final UserProfile userProfile = new UserProfile();

    @Autowired
    private CalendarEventRepository calendarEventRepository;
    @Autowired
    private ICSParser icsParser;

    @GetMapping("/profile")
    public String profilePage(Model model) {

        // ---- work window ----
        String workStart = "09:00";
        String workEnd = "17:00";

        if (userProfile.getWorkingWindows() != null && !userProfile.getWorkingWindows().isEmpty()) {
            UserProfile.WorkingWindow w = userProfile.getWorkingWindows().get(0);
            if (w.getStart() != null) workStart = w.getStart().toString();
            if (w.getEnd() != null) workEnd = w.getEnd().toString();
        }

        model.addAttribute("workStart", workStart);
        model.addAttribute("workEnd", workEnd);

        // ---- productivity ----
        if (userProfile.getProductivity() == null) {
            userProfile.setProductivity(new Productivity());
        }
        model.addAttribute("productivity", userProfile.getProductivity());

        return "profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@RequestParam String workStart,
                              @RequestParam String workEnd,
                              @RequestParam Double morning,
                              @RequestParam Double afternoon,
                              @RequestParam Double evening,
                              @RequestParam Double night) {

        userProfile.setProductivity(new Productivity(morning, afternoon, evening, night));

        userProfile.setWorkingWindows(List.of(
                userProfile.new WorkingWindow(
                        LocalTime.parse(workStart),
                        LocalTime.parse(workEnd)
                )
        ));

        return "redirect:/profile";
    }

    @PostMapping("/profile/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            return "redirect:/profile?error=empty";
        }
        try {
            InputStream inputStream = file.getInputStream();
            List<CalendarEvent> importedEvents = icsParser.parseICSStream(inputStream);
            for (CalendarEvent event : importedEvents) {
                calendarEventRepository.save(event);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/profile";
    }

}
