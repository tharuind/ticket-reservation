package com.example.ticket_reservation.service;

import java.math.BigDecimal;
import com.example.ticket_reservation.dto.StatsResponse;
import com.example.ticket_reservation.model.ReservationStatus;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;
import com.example.ticket_reservation.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;

    public AdminService(
            UserRepository userRepository,
            EventRepository eventRepository,
            ReservationRepository reservationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
    }

    public StatsResponse statistics() {
        var confirmed = reservationRepository.findAll().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.CONFIRMED)
                .toList();
        long ticketsSold = confirmed.stream().mapToLong(reservation -> reservation.getSeats()).sum();
        BigDecimal revenue = confirmed.stream().map(reservation -> reservation.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new StatsResponse(userRepository.count(), eventRepository.count(), ticketsSold, revenue);
    }
}
