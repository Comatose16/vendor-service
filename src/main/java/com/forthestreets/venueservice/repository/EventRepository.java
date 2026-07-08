package com.forthestreets.venueservice.repository;

import com.forthestreets.venueservice.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Pulls all events that are currently active right now based on system time.
     */
    @Query("SELECT e FROM Event e WHERE e.startTime <= CURRENT_TIMESTAMP AND e.endTime >= CURRENT_TIMESTAMP")
    List<Event> findAllActiveEventsNow();
}