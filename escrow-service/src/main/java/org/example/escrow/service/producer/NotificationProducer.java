package org.example.escrow.service.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.notification.NotificationMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;
    private final AppProperties appProperties;

    public void sendNotification(NotificationMessage message) {
        log.info("Queuing notification for: {}", message.getRecipient());
        rabbitTemplate.convertAndSend(
                appProperties.getRabbitmq().getExchange(),
                appProperties.getRabbitmq().getRoutingKey(),
                message
        );
    }
}