package com.example.ticket_reservation.dto;

import java.math.BigDecimal;
import java.time.Instant;
import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.ReservationStatus;

public record ReservationResponse(
        String id,
        String reservationCode,
        String eventId,
        String customerUsername,
        int seats,
        BigDecimal amount,
        ReservationStatus status,
        Instant createdAt) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(), reservation.getReservationCode(), reservation.getEventId(),
                reservation.getCustomerUsername(), reservation.getSeats(), reservation.getAmount(),
                reservation.getStatus(), reservation.getCreatedAt());
    }
}
