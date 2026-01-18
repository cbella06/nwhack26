package com.example.scheduler;

import jakarta.persistence.Embeddable;
import java.time.LocalTime;

@Embeddable
public class Productivity {
    private Double morning = 1.0;
    private Double afternoon = 1.0;
    private Double evening = 1.0;
    private Double night = 1.0;

    public enum DayPart { MORNING, AFTERNOON, EVENING, NIGHT }

    public Productivity() {}

    public Productivity(Double morning, Double afternoon, Double evening, Double night) {
        this.morning = morning;
        this.afternoon = afternoon;
        this.evening = evening;
        this.night = night;
    }

    public static DayPart dayPart(LocalTime t) {
        int h = t.getHour();
        if (h >= 5 && h < 12) return DayPart.MORNING;
        if (h >= 12 && h < 17) return DayPart.AFTERNOON;
        if (h >= 17 && h < 21) return DayPart.EVENING;
        return DayPart.NIGHT; // 21–23 and 0–4
    }

    // Getters and Setters
    public Double getMorning() { return morning; }
    public void setMorning(Double morning) { this.morning = morning; }

    public Double getAfternoon() { return afternoon; }
    public void setAfternoon(Double afternoon) { this.afternoon = afternoon; }

    public Double getEvening() { return evening; }
    public void setEvening(Double evening) { this.evening = evening; }

    public Double getNight() { return night; }
    public void setNight(Double night) { this.night = night; }

    public static double productivityWeight(Productivity p, LocalTime slotStart) {
        return switch (dayPart(slotStart)) {
            case MORNING -> p.getMorning();
            case AFTERNOON -> p.getAfternoon();
            case EVENING -> p.getEvening();
            case NIGHT -> p.getNight();
        };
    }

}