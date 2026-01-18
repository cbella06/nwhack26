package com.example.scheduler;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(unique = true)
    private String username;

    @OneToMany(mappedBy = "userProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<WorkingWindow> workingWindows = new ArrayList<>();

    @Embedded
    private Productivity productivity = new Productivity();

    public UserProfile() {}

    public UUID getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<WorkingWindow> getWorkingWindows() { return workingWindows; }

    // KEY FIX: always set back-reference
    public void setWorkingWindows(List<WorkingWindow> windows) {
        this.workingWindows.clear();
        if (windows != null) {
            for (WorkingWindow w : windows) {
                w.setUserProfile(this);
                this.workingWindows.add(w);
            }
        }
    }

    // Optional helper (nice to use in controller)
    public void addWorkingWindow(WorkingWindow w) {
        w.setUserProfile(this);
        this.workingWindows.add(w);
    }

    public Productivity getProductivity() { return productivity; }
    public void setProductivity(Productivity productivity) {
        this.productivity = (productivity != null) ? productivity : new Productivity();
    }
}
