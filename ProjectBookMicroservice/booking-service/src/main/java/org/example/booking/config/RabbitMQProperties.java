package org.example.booking.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rabbitmq")
@Data
public class RabbitMQProperties {
    /**
     * Maps to rabbitmq.exchange
     */
    private String exchange;

    /**
     * Maps to rabbitmq.queue
     */
    private String queue;

    /**
     * Maps to rabbitmq.routing-key (Spring automatically converts kebab-case to camelCase)
     */
    private String routingKey;
}