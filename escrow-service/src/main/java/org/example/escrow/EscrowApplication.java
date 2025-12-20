package org.example.escrow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class EscrowApplication {

    private static final Logger logger = LoggerFactory.getLogger(EscrowApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EscrowApplication.class, args);
        logger.info("Escrow Service Backend is running. Profile: Production-Simulation");
    }
}