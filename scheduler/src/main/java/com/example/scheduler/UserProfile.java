package com.example.scheduler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;


public class UserProfile {
    public enum TieBreakerRule {
        EARLIEST_DUE,
        HIGHEST_IMPORTANCE,
        SHORTEST_TASK,
        LONGEST_TASK
    }

    private String username;
    private UUID userId;
    private List<WorkingWindow> workingWindows;
    private Productivity productivity;
    private TieBreakerRule tieBreakerRule = TieBreakerRule.EARLIEST_DUE;

    public UserProfile() {
        this.userId = UUID.randomUUID();
        this.workingWindows = new ArrayList<>();
        this.productivity = new Productivity();
    }

    public UserProfile(String username, List<WorkingWindow> workingWindows,
                       Productivity productivity, TieBreakerRule tieBreakerRule) {
        this.username = username;
        this.userId = UUID.randomUUID();
        this.workingWindows = (workingWindows != null)
                ? workingWindows
                : new ArrayList<>();
        this.productivity = (productivity != null)
                ? productivity
                : new Productivity();
        this.tieBreakerRule = (tieBreakerRule != null)
                ? tieBreakerRule
                : TieBreakerRule.EARLIEST_DUE;
    }

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public List<WorkingWindow> getWorkingWindows() { return workingWindows; }
    public void setWorkingWindows(List<WorkingWindow> workingWindows) { this.workingWindows = workingWindows; }

    public Productivity getProductivity() { return productivity; }
    public void setProductivity(Productivity productivity) { this.productivity = productivity; }

    public TieBreakerRule getTieBreakerRule() { return tieBreakerRule; }
    public void setTieBreakerRule(TieBreakerRule tieBreakerRule) { this.tieBreakerRule = tieBreakerRule; }

    public class  WorkingWindow {
        private LocalTime start;
        private LocalTime end;

        public WorkingWindow() {}

        public WorkingWindow(LocalTime start, LocalTime end) {
            this.start = start;
            this.end = end;
        }

        // Getters and Setters
        public LocalTime getStart() { return start; }
        public void setStart(LocalTime start) { this.start = start; }

        public LocalTime getEnd() { return end; }
        public void setEnd(LocalTime end) { this.end = end; }
    }
}