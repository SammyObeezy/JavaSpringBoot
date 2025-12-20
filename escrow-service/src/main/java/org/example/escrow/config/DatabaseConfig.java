package org.example.escrow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaAuditing // Required for CreatedAt/UpdatedAt to work automatically
@EnableTransactionManagement // Enables @Transactional annotation
public class DatabaseConfig {
    // Spring Boot auto-configures the DataSource based on application.properties.
    // We enable Auditing and Transaction Management here to keep the Main class clean.
}