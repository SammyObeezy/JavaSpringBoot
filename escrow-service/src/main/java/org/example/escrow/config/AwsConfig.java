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
    public SnsClient snsClient(){
        AppProperties.Aws awsProps = appProperties.getAws();

        return SnsClient.builder()
                .region(Region.of(awsProps.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsProps.getAccessKeyId(),
                                awsProps.getSecretAccessKey()
                        )
                ))
                .build();
    }
}
