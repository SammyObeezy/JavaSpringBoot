package org.example.escrow.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Fixed: Removed @Testcontainers annotation to prevent JUnit from managing lifecycle (stopping it too early)
public abstract class AbstractIntegrationTest {

    // Define container, but do not use @Container annotation
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_escrow_db")
            .withUsername("test")
            .withPassword("test");

    static {
        // Start Manually: Ensures container starts once and stays running for all integration tests
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Ensure Hibernate creates tables for the test DB
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}