package com.example.ticket_reservation.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import com.example.ticket_reservation.model.AppUser;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.Role;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.UserRepository;

@Component
public class DataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    private final MongoTemplate mongoTemplate;

    public DataLoader(
            UserRepository userRepository,
            EventRepository eventRepository,
            PasswordEncoder passwordEncoder,
            MongoTemplate mongoTemplate) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        seedUser("admin", "admin123", "System Administrator", Role.ADMIN);
        seedUser("organizer", "organizer123", "Event Organizer", Role.ORGANIZER);
        seedUser("customer", "customer123", "Sample Customer", Role.CUSTOMER);
        if (eventRepository.count() == 0) {
            seedEvent("Summer Music Festival", "Live music and entertainment", "Colombo",
                    LocalDateTime.now().plusDays(30), 200, new BigDecimal("2500.00"), "organizer");
            seedEvent("Technology Conference", "A day of technology talks", "Kandy",
                    LocalDateTime.now().plusDays(60), 100, new BigDecimal("1500.00"), "organizer");
        }
        if (!mongoTemplate.collectionExists("reservations")) {
            mongoTemplate.createCollection(Reservation.class);
        }
    }

    private void seedUser(String username, String password, String fullName, Role role) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new AppUser(username, passwordEncoder.encode(password), fullName, role));
        }
    }

    private void seedEvent(
            String name, String description, String venue, LocalDateTime eventDate,
            int seats, BigDecimal ticketPrice, String organizer) {
        Event event = new Event();
        event.setName(name);
        event.setDescription(description);
        event.setVenue(venue);
        event.setEventDate(eventDate);
        event.setTotalSeats(seats);
        event.setAvailableSeats(seats);
        event.setTicketPrice(ticketPrice);
        event.setOrganizer(organizer);
        eventRepository.save(event);
    }
}
