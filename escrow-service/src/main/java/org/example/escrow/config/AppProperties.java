package org.example.escrow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Type-safe configuration loaded from application.properties.
 * Use this bean in your Services instead of static constants.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app.config")
public class AppProperties {

    private final Security security = new Security();
    private final Aws aws = new Aws();
    private final Escrow escrow = new Escrow();
    private final Defaults defaults = new Defaults();
    private final Api api = new Api();

    @Data
    public static class Security {
        private long otpExpirationMinutes;
        private int maxLoginAttempts;
        //32-byte AES Key (Base64 encoded)
        private String encryptionKey;
    }

    @Data
    public static class Aws{
        private String accessKeyId;
        private String secretAccessKey;
        private String region;
        private String snsSenderId;
    }

    @Data
    public static class Escrow {
        private double platformFeePercentage;
        private String defaultCurrency;
    }

    @Data
    public static class Defaults {
        private int pageNumber;
        private int pageSize;
        private String sortBy;
        private String sortDirection;
    }

    @Data
    public static class Api {
        private String prefix;
    }
}