package com.example.ticket_reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.example.ticket_reservation.model.Event;

public record EventResponse(
        String id,
        String name,
        String description,
        String venue,
        LocalDateTime eventDate,
        int totalSeats,
        int availableSeats,
        BigDecimal ticketPrice,
        String organizer) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getId(), event.getName(), event.getDescription(), event.getVenue(),
                event.getEventDate(), event.getTotalSeats(), event.getAvailableSeats(),
                event.getTicketPrice(), event.getOrganizer());
    }
}
