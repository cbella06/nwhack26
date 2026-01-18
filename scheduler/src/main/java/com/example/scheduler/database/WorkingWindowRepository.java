package com.example.scheduler.database;

import com.example.scheduler.WorkingWindow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface WorkingWindowRepository extends JpaRepository<WorkingWindow, UUID> {
    List<WorkingWindow> findByUserProfile_Id(UUID userProfileId);
}
