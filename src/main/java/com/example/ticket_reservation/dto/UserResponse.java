package com.example.ticket_reservation.dto;

import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Role;

public record UserResponse(String id, String username, String fullName, Role role) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }
}
