package com.example.ticket_reservation.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.example.ticket_reservation.dto.ReservationRequest;
import com.example.ticket_reservation.dto.ReservationResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.model.Reservation;
import com.example.ticket_reservation.model.ReservationStatus;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final EventRepository eventRepository;
    private final EventService eventService;
    private final MongoTemplate mongoTemplate;

    public ReservationService(
            ReservationRepository reservationRepository,
            EventRepository eventRepository,
            EventService eventService,
            MongoTemplate mongoTemplate) {
        this.reservationRepository = reservationRepository;
        this.eventRepository = eventRepository;
        this.eventService = eventService;
        this.mongoTemplate = mongoTemplate;
    }

    public ReservationResponse book(ReservationRequest request, String username) {
        Event event = eventService.takeSeats(request.eventId(), request.seats());
        Reservation reservation = new Reservation();
        reservation.setReservationCode("TKT-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase());
        reservation.setCustomerUsername(username);
        reservation.setEventId(event.getId());
        reservation.setSeats(request.seats());
        reservation.setAmount(event.getTicketPrice().multiply(java.math.BigDecimal.valueOf(request.seats())));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservation.setCreatedAt(Instant.now());
        try {
            return ReservationResponse.from(reservationRepository.save(reservation));
        } catch (DataAccessException exception) {
            eventService.releaseSeats(event.getId(), request.seats());
            throw exception;
        }
    }

    public List<ReservationResponse> mine(String username) {
        return reservationRepository.findByCustomerUsername(username).stream()
                .map(ReservationResponse::from).toList();
    }

    public List<ReservationResponse> allFor(String username, boolean admin) {
        if (admin) {
            return reservationRepository.findAll().stream().map(ReservationResponse::from).toList();
        }
        List<String> eventIds = eventRepository.findByOrganizer(username).stream().map(Event::getId).toList();
        return eventIds.isEmpty()
                ? List.of()
                : reservationRepository.findByEventIdIn(eventIds).stream().map(ReservationResponse::from).toList();
    }

    public void cancel(String id, String username, boolean admin) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Reservation not found"));
        if (!admin && !reservation.getCustomerUsername().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only cancel your own reservations");
        }
        Query query = Query.query(Criteria.where("_id").is(id).and("status").is(ReservationStatus.CONFIRMED));
        Reservation cancelled = mongoTemplate.findAndModify(
                query, Update.update("status", ReservationStatus.CANCELLED),
                FindAndModifyOptions.options().returnNew(true), Reservation.class);
        if (cancelled == null) {
            throw new ApiException(HttpStatus.CONFLICT, "Reservation has already been cancelled");
        }
        eventService.releaseSeats(cancelled.getEventId(), cancelled.getSeats());
    }
}
