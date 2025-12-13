package org.example.payment.consumer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class PaymentConsumer {

    // Listen to the queue defined in application.properties
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void consumePaymentEvent(Map<String, Object> message){
        log.info("=========================================");
        log.info("PAYMENT REQUEST RECEIVED");
        log.info("Booking ID: {}", message.get("bookingId"));
        log.info("Amount: KES {}", message.get("amount"));
        log.info("User Phone: {}", message.get("phoneNumber"));

        // Simulation of Daraja/M-Pesa API Call
        processPayment(message);
        log.info("=========================================");
    }

    private void processPayment(Map<String, Object> message) {
        try {
            log.info("Initiating M-Pesa STK Push to {}...", message.get("phoneNumber"));
            // Simulate processing time
            Thread.sleep(2000);
            log.info("Payment Successful! Transaction ID: REF123456");

            // TODO: In Phase 8, we will send a message BACK to Booking Service
            // to update status from PENDING_PAYMENT to CONFIRMED.
        } catch (InterruptedException e) {
            log.error("Payment processing failed", e);
        }
    }
}
