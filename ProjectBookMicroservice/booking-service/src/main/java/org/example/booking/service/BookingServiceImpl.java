package org.example.booking.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.booking.client.UserClient;
import org.example.booking.config.RabbitMQProperties;
import org.example.booking.dto.BookingRequest;
import org.example.booking.dto.UserDTO;
import org.example.booking.model.Booking;
import org.example.booking.model.Event;
import org.example.booking.model.enums.BookingStatus;
import org.example.booking.repository.BookingRepository;
import org.example.booking.repository.EventRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties rabbitMQProperties;
    private final UserClient userClient;

    @Override
    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequest request) {
        log.info("Processing booking for User: {}", request.getUserId());

        // 1. Validate Event
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 2. Check Availability
        if (!event.hasTickets(request.getTicketCount())) {
            throw new RuntimeException("Not enough tickets available");
        }

        // 3. Calculate Price
        BigDecimal totalPrice = event.getTicketPrice().multiply(BigDecimal.valueOf(request.getTicketCount()));

        // 4. Create Booking (PENDING state)
        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setEvent(event);
        booking.setTicketCount(request.getTicketCount());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING_PAYMENT);

        // 5. Update Inventory
        event.setAvailableTickets(event.getAvailableTickets() - request.getTicketCount());
        eventRepository.save(event);
        Booking savedBooking = bookingRepository.save(booking);

        // 6. Send to RabbitMQ
        publishPaymentEvent(savedBooking);

        return savedBooking;
    }

    private void publishPaymentEvent(Booking booking) {
        try {
            // 2. FIX: Fetch real user details instead of hardcoding
            String userPhoneNumber = fetchUserPhoneNumber(booking.getUserId());

            Map<String, Object> message = new HashMap<>();
            message.put("bookingId", booking.getId());
            message.put("userId", booking.getUserId());
            message.put("amount", booking.getTotalPrice());
            message.put("phoneNumber", userPhoneNumber);

            // Use the injected properties object
            rabbitTemplate.convertAndSend(
                    rabbitMQProperties.getExchange(),
                    rabbitMQProperties.getRoutingKey(),
                    message
            );

            log.info("Booking Event published to RabbitMQ for Booking ID: {}", booking.getId());
        } catch (Exception e) {
            log.error("Failed to publish to RabbitMQ", e);
        }
    }

    /**
     * Helper to get User details.
     * In the next step (Phase 5), we will replace this with a 'FeignClient'
     * call to the Auth Service to get the real number.
     */
    private String fetchUserPhoneNumber(Long userId) {
        try{
            UserDTO user = userClient.getUserById(userId);
            return user.getPhoneNumber();
        } catch (Exception e){
            log.error("Could not fetch user details for ID: {}", userId, e);
            return "UNKNOWN";
        }
    }
}