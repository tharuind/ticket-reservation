package com.example.ticket_reservation.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.example.ticket_reservation.model.Reservation;

public interface ReservationRepository extends MongoRepository<Reservation, String> {
    List<Reservation> findByCustomerUsername(String customerUsername);
    List<Reservation> findByEventId(String eventId);
    List<Reservation> findByEventIdIn(List<String> eventIds);
    boolean existsByEventId(String eventId);
}
