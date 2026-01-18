package com.example.scheduler.database;

import com.example.scheduler.CalendarEvent; // Ensure this points to your Entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CalendarEventRepository extends JpaRepository<CalendarEvent, Long> {
    List<CalendarEvent> findByDate(LocalDate date);
}
