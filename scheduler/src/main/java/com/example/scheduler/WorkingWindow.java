package com.example.scheduler;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Represents the User's working window, which are the hours of the day they are available to work.
 */
@Entity
@Table(name = "working_windows")
public class WorkingWindow {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    public WorkingWindow() {}

    public WorkingWindow(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public UUID getId() { return id; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
}
