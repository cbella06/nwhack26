package com.example.scheduler;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Summary;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ICSParser {

    /**
     * Parse an ICS file and extract calendar events
     * Only includes events with specific start and end times
     * Skips recurring events, all-day events, and multi-day events
     * @param input InputStream of the .ics file
     * @return List of CalendarEvent objects
     */
    public List<CalendarEvent> parseICSStream(InputStream input) {
        List<CalendarEvent> events = new ArrayList<>();

        try {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(input);

            System.out.println("DEBUG ICSParser: Successfully built calendar");
            System.out.println("DEBUG ICSParser: Calendar has " +
                    calendar.getComponents(Component.VEVENT).size() +
                    " VEVENT components");

            for (Object component : calendar.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) component;

                // Skip recurring events
                if (event.getProperty("RRULE") != null) {
                    System.out.println("DEBUG ICSParser: Skipping recurring event");
                    continue;
                }

                String title = getEventTitle(event);
                LocalDateTime startDateTime = getEventStart(event);
                LocalDateTime endDateTime = getEventEnd(event);

                System.out.println("DEBUG ICSParser: Processing event: " + title);
                System.out.println("DEBUG ICSParser: Start: " + startDateTime);
                System.out.println("DEBUG ICSParser: End: " + endDateTime);

                if (startDateTime == null || endDateTime == null) {
                    System.out.println("DEBUG ICSParser: Skipping - no start/end times");
                    continue;
                }

                // Create CalendarEvent from the parsed data
                CalendarEvent calEvent = new CalendarEvent(
                        startDateTime.toLocalDate(),    // date
                        startDateTime.toLocalTime(),     // start time
                        endDateTime.toLocalTime(),       // end time
                        title                            // title
                );

                events.add(calEvent);
                System.out.println("DEBUG ICSParser: Added event: " + title +
                        " on " + startDateTime.toLocalDate() +
                        " from " + startDateTime.toLocalTime() +
                        " to " + endDateTime.toLocalTime());
            }

        } catch (Exception e) {
            System.err.println("ERROR in ICSParser:");
            e.printStackTrace();
        }

        System.out.println("DEBUG ICSParser: Returning " + events.size() + " total events");
        return events;
    }

    /**
     * Get event title/summary
     */
    private String getEventTitle(VEvent event) {
        Summary summary = event.getSummary();
        return summary != null ? summary.getValue() : "Untitled Event";
    }

    /**
     * Get event start time
     */
    private LocalDateTime getEventStart(VEvent event) {
        DtStart dtStart = event.getStartDate();
        if (dtStart == null) return null;

        Date startDate = dtStart.getDate();
        return convertToLocalDateTime(startDate);
    }

    /**
     * Get event end time
     */
    private LocalDateTime getEventEnd(VEvent event) {
        DtEnd dtEnd = event.getEndDate();
        if (dtEnd == null) return null;

        Date endDate = dtEnd.getDate();
        return convertToLocalDateTime(endDate);
    }

    /**
     * Convert java.util.Date to LocalDateTime using system default timezone
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}