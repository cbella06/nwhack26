package com.example.scheduler;

import com.example.scheduler.database.CalendarEventRepository;
import com.example.scheduler.database.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;

/**
 * Controller for user profile. Saves user data to database and handles ICS imports.
 */
@Controller
public class ProfileController {

    @Autowired private UserProfileRepository userProfileRepository;
    @Autowired private CalendarEventRepository calendarEventRepository;
    @Autowired private ICSParser icsParser;

    @GetMapping("/profile")
    public String profilePage(Model model) {
        UserProfile profile = userProfileRepository
                .findByUsername("default")
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUsername("default");
                    return userProfileRepository.save(p);
                });

        String workStart = "09:00";
        String workEnd = "17:00";

        if (!profile.getWorkingWindows().isEmpty()) {
            WorkingWindow w = profile.getWorkingWindows().get(0);
            if (w.getStartTime() != null) workStart = w.getStartTime().toString();
            if (w.getEndTime() != null) workEnd = w.getEndTime().toString();
        }

        if (profile.getProductivity() == null) {
            profile.setProductivity(new Productivity());
            userProfileRepository.save(profile);
        }

        model.addAttribute("workStart", workStart);
        model.addAttribute("workEnd", workEnd);
        model.addAttribute("productivity", profile.getProductivity());
        return "profile";
    }

    @PostMapping("/profile/save")
    public String saveProfile(@RequestParam String workStart,
                              @RequestParam String workEnd,
                              @RequestParam Double morning,
                              @RequestParam Double afternoon,
                              @RequestParam Double evening,
                              @RequestParam Double night) {

        System.out.println("DEBUG: /profile/save called");
        System.out.println("DEBUG: workStart=" + workStart + ", workEnd=" + workEnd);
        System.out.println("DEBUG: productivity = "
                + morning + ", " + afternoon + ", " + evening + ", " + night);

        UserProfile profile = userProfileRepository
                .findByUsername("default")
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setUsername("default");
                    return userProfileRepository.save(p);
                });

        profile.setProductivity(new Productivity(morning, afternoon, evening, night));

        profile.getWorkingWindows().clear();
        profile.addWorkingWindow(new WorkingWindow(LocalTime.parse(workStart), LocalTime.parse(workEnd)));

        userProfileRepository.save(profile);
        return "redirect:/profile";
    }

    @PostMapping("/profile/import")
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        System.err.println("!!!!!! FILE UPLOAD ENDPOINT HIT !!!!!");

        System.out.println("========================================");
        System.out.println("DEBUG: handleFileUpload TRIGGERED!!!");
        System.out.println("DEBUG: File name: " + file.getOriginalFilename());
        System.out.println("DEBUG: File size: " + file.getSize() + " bytes");
        System.out.println("DEBUG: Content type: " + file.getContentType());
        System.out.println("DEBUG: Is empty? " + file.isEmpty());
        System.out.println("========================================");

        if (file.isEmpty()) {
            System.out.println("ERROR: File is empty!");
            return "redirect:/profile?error=empty";
        }

        try {
            System.out.println("DEBUG: About to parse ICS file...");
            List<CalendarEvent> importedEvents = icsParser.parseICSStream(file.getInputStream());
            System.out.println("DEBUG: Parser returned " + importedEvents.size() + " events");

            if (importedEvents.isEmpty()) {
                System.out.println("WARNING: Parser found 0 events in the file!");
                return "redirect:/profile?error=noevents";
            }

            System.out.println("DEBUG: Deleting existing calendar events...");
            calendarEventRepository.deleteAll();

            System.out.println("DEBUG: Saving " + importedEvents.size() + " new events...");
            for (int i = 0; i < importedEvents.size(); i++) {
                CalendarEvent event = importedEvents.get(i);
                System.out.println("DEBUG: Saving event " + (i+1) + ": " +
                        event.getTitle() +
                        " on " + event.getDate() +
                        " from " + event.getStartTime() +
                        " to " + event.getEndTime());
                calendarEventRepository.save(event);
            }

            System.out.println("DEBUG: All events saved successfully!");

        } catch (Exception e) {
            System.err.println("========================================");
            System.err.println("CRITICAL ERROR DURING IMPORT:");
            System.err.println("Error type: " + e.getClass().getName());
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            System.err.println("========================================");
            return "redirect:/profile?error=exception";
        }

        return "redirect:/profile?success=imported";
    }
}