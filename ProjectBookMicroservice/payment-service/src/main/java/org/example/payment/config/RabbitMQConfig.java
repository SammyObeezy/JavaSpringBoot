package org.example.payment.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // This bean tells Spring: "When you receive a message, use Jackson to turn JSON into Java Objects"
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue paymentQueue() {
        // name: "payment.queue", durable: true
        return new Queue("payment.queue", true);
    }
}