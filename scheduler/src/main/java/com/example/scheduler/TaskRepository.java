package com.example.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    // Find incomplete tasks
    List<Task> findByDoneFalse();

    // Find completed tasks
    List<Task> findByDoneTrue();

    // Find tasks by tag
    List<Task> findByTagsContaining(String tag);

    // Find overdue incomplete tasks
    List<Task> findByDoneFalseAndDueDateTimeBefore(LocalDateTime dateTime);
}