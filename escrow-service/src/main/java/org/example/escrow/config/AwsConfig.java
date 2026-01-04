package org.example.escrow.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    private final AppProperties appProperties;

    @Bean
    public SnsClient snsClient() {
        // 1. Extract credentials from AppProperties
        String accessKey = appProperties.getAws().getAccessKeyId();
        String secretKey = appProperties.getAws().getSecretAccessKey();
        String regionString = appProperties.getAws().getRegion();

        // 2. Validate (Fail fast if missing or default)
        // This helps debug configuration issues immediately on startup
        if (accessKey == null || accessKey.contains("ChangeMe") || secretKey == null) {
            throw new RuntimeException("Invalid AWS Credentials in application.properties. Please configure app.config.aws.*");
        }

        // 3. Build the Client
        return SnsClient.builder()
                .region(Region.of(regionString))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)
                ))
                .build();
    }
}