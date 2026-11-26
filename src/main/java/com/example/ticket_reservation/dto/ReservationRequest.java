package com.example.ticket_reservation.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReservationRequest(
        @NotBlank String eventId,
        @Min(1) @Max(10) int seats) {
}
