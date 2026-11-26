package com.example.ticket_reservation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EventRequest(
        @NotBlank String name,
        String description,
        @NotBlank String venue,
        @NotNull @Future LocalDateTime eventDate,
        @Min(1) int totalSeats,
        @NotNull @DecimalMin("0.0") BigDecimal ticketPrice) {
}
