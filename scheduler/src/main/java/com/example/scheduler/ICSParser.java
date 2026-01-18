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

            // Get all VEVENT components
            for (Object component : calendar.getComponents(Component.VEVENT)) {
                VEvent event = (VEvent) component;

                // Extract event details
                String title = getEventTitle(event);
                LocalDateTime startDateTime = getEventStart(event);
                LocalDateTime endDateTime = getEventEnd(event);

                if (startDateTime != null && endDateTime != null) {
                    // Convert to CalendarEvent
                    CalendarEvent calEvent = new CalendarEvent(
                            startDateTime.toLocalDate(),
                            startDateTime.toLocalTime(),
                            endDateTime.toLocalTime(),
                            title
                    );
                    calEvent.setAsFixedEvent();
                    events.add(calEvent);
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing ICS file: " + e.getMessage());
            e.printStackTrace();
        }

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