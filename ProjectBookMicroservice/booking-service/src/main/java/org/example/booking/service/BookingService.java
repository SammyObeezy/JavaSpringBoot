package org.example.booking.service;

import org.example.booking.dto.BookingRequest;
import org.example.booking.model.Booking;
import org.example.booking.model.Event;

public interface BookingService {
    Event createEvent(Event event); // Admin only usually
    Booking createBooking(BookingRequest request);
}
