package com.example.ticket_reservation.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import com.example.ticket_reservation.dto.EventRequest;
import com.example.ticket_reservation.dto.EventResponse;
import com.example.ticket_reservation.exception.ApiException;
import com.example.ticket_reservation.model.Event;
import com.example.ticket_reservation.repository.EventRepository;
import com.example.ticket_reservation.repository.ReservationRepository;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final MongoTemplate mongoTemplate;

    public EventService(
            EventRepository eventRepository,
            ReservationRepository reservationRepository,
            MongoTemplate mongoTemplate) {
        this.eventRepository = eventRepository;
        this.reservationRepository = reservationRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<EventResponse> list(String name) {
        List<Event> events = name == null || name.isBlank()
                ? eventRepository.findAll()
                : eventRepository.findByNameContainingIgnoreCase(name);
        return events.stream().map(EventResponse::from).toList();
    }

    public EventResponse get(String id) {
        return EventResponse.from(findEvent(id));
    }

    public EventResponse create(EventRequest request, String organizer) {
        Event event = new Event();
        applyRequest(event, request);
        event.setAvailableSeats(request.totalSeats());
        event.setOrganizer(organizer);
        return EventResponse.from(eventRepository.save(event));
    }

    public EventResponse update(String id, EventRequest request, String username, boolean admin) {
        Event event = findEvent(id);
        if (!admin && !event.getOrganizer().equals(username)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only update your own events");
        }
        int reservedSeats = event.getTotalSeats() - event.getAvailableSeats();
        if (request.totalSeats() < reservedSeats) {
            throw new ApiException(HttpStatus.CONFLICT, "Total seats cannot be less than seats already reserved");
        }
        applyRequest(event, request);
        event.setAvailableSeats(request.totalSeats() - reservedSeats);
        return EventResponse.from(eventRepository.save(event));
    }

    public void delete(String id) {
        Event event = findEvent(id);
        if (reservationRepository.existsByEventId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "An event with reservations cannot be deleted");
        }
        eventRepository.delete(event);
    }

    public Event takeSeats(String id, int seats) {
        Query query = Query.query(Criteria.where("_id").is(id).and("availableSeats").gte(seats));
        Event updated = mongoTemplate.findAndModify(
                query, new Update().inc("availableSeats", -seats),
                FindAndModifyOptions.options().returnNew(true), Event.class);
        if (updated != null) {
            return updated;
        }
        if (!eventRepository.existsById(id)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Event not found");
        }
        throw new ApiException(HttpStatus.CONFLICT, "Not enough seats are available");
    }

    public void releaseSeats(String id, int seats) {
        Query query = Query.query(Criteria.where("_id").is(id));
        Event updated = mongoTemplate.findAndModify(
                query, new Update().inc("availableSeats", seats),
                FindAndModifyOptions.options().returnNew(true), Event.class);
        if (updated == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Event not found");
        }
    }

    private Event findEvent(String id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Event not found"));
    }

    private void applyRequest(Event event, EventRequest request) {
        event.setName(request.name());
        event.setDescription(request.description());
        event.setVenue(request.venue());
        event.setEventDate(request.eventDate());
        event.setTotalSeats(request.totalSeats());
        event.setTicketPrice(request.ticketPrice());
    }
}
