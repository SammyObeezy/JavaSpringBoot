package org.example.escrow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EscrowApplication {

    private static final Logger logger = LoggerFactory.getLogger(EscrowApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EscrowApplication.class, args);
        logger.info("Escrow Service Backend is running. Profile: Production-Simulation");
    }
}