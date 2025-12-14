package org.example.payment.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.model.Transaction;
import org.example.payment.model.PaymentStatus;
import org.example.payment.repository.TransactionRepository;
import org.example.payment.service.MpesaService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentConsumer {

    private final TransactionRepository transactionRepository;
    private final MpesaService mpesaService; // Inject the real service

    // Listen to the queue defined in application.properties
    @RabbitListener(queues = "${rabbitmq.queue}")
    public void consumePaymentEvent(Map<String, Object> message){
        log.info("PAYMENT EVENT RECEIVED: {}", message);

        try {
            // 1. Extract Data
            Long bookingId = ((Number) message.get("bookingId")).longValue();
            Long userId = ((Number) message.get("userId")).longValue();
            BigDecimal amount = new BigDecimal(message.get("amount").toString());
            String phoneNumber = (String) message.get("phoneNumber");

            // 2. Create Transaction Record (PENDING)
            Transaction transaction = new Transaction();
            transaction.setBookingId(bookingId);
            transaction.setUserId(userId);
            transaction.setAmount(amount);
            transaction.setPhoneNumber(phoneNumber);
            transaction.setStatus(PaymentStatus.PENDING);

            transactionRepository.save(transaction);
            log.info("Transaction saved: [Booking ID: {}] [Status: PENDING]", bookingId);

            // 3. Initiate Real M-Pesa Payment
            mpesaService.initiateStkPush(transaction);
        } catch (Exception e) {
            log.error("Error processing payment message", e);
        }
    }
}
