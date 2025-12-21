package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
@RequiredArgsConstructor
public class AwsSnsService implements NotificationService{

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsService.class);
    private final SnsClient snsClient;
    private final AppProperties appProperties;

    @Override
    public void sendSms(String phoneNumber, String message){
        try{
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    // Optional: Set SenderID (e.g., "EscrowApp") if supported in region
                    // .messageAttributes(...)
                    .build();

            PublishResponse response = snsClient.publish(request);
            logger.info("OTP sent to {} via AWS SNS. MessageId: {}", phoneNumber, response.messageId());
        } catch (SnsException e){
            logger.error("Failed to send SMS via AWS SNS: {}", e.awsErrorDetails().errorMessage());
            // In a real app, you might want to throw a custom exception or fallback
            throw new RuntimeException("SMS Sending Failed");
        }
    }
}
