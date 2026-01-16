package org.example.escrow.service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.dto.notification.NotificationMessage;
import org.example.escrow.model.enums.NotificationChannel;
import org.example.escrow.service.EmailService;
import org.example.escrow.service.NotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService smsService;
    private final EmailService emailService;

    @RabbitListener(queues = "${app.config.rabbitmq.queue}")
    public void consumeMessage(NotificationMessage message) {
        log.info("Received Queue Message. Channel: {}", message.getChannel());

        try {
            if (message.getChannel() == NotificationChannel.EMAIL) {
                emailService.sendEmail(message.getRecipient(), "Escrow Notification", message.getContent());
            } else {
                smsService.sendSms(message.getRecipient(), message.getContent());
            }
        } catch (Exception e) {
            log.error("Failed to process notification: {}", e.getMessage());
            // In production, consider throwing a custom exception to send this to a Dead Letter Queue (DLQ)
        }
    }
}