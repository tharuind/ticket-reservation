package com.example.ticket_reservation.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.ticket_reservation.model.Event;

public interface EventRepository extends MongoRepository<Event, String> {
    List<Event> findByNameContainingIgnoreCase(String name);
    List<Event> findByOrganizer(String organizer);
}
