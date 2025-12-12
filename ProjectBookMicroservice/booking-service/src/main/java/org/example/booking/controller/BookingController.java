package org.example.booking.controller;

import lombok.RequiredArgsConstructor;
import org.example.booking.dto.BookingRequest;
import org.example.booking.model.Booking;
import org.example.booking.model.Event;
import org.example.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody Event event) {
        // Admin endpoint to seed data
        return ResponseEntity.ok(bookingService.createEvent(event));
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody BookingRequest request) {
        return ResponseEntity.ok(bookingService.createBooking(request));
    }
}