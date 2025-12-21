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
public class AwsSnsService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsService.class);
    private final SnsClient snsClient;
    private final AppProperties appProperties;

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(phoneNumber)
                    .build();

            PublishResponse response = snsClient.publish(request);
            logger.info("OTP sent to {} via AWS SNS. MessageId: {}", phoneNumber, response.messageId());

        } catch (Exception e) {
            // DEV MODE FIX: If AWS fails (invalid keys, sandbox issues), don't crash the app.
            // Instead, log the OTP so the developer can still test the flow.
            // We catch generic 'Exception' here which covers SnsException.
            logger.error("AWS SNS Failed. FALLBACK LOGGING -> To: {}, Message: {}", phoneNumber, message);

            // In Production, you might want to throw this.
            // For now, we suppress it so you can register without valid AWS keys.
            // throw new RuntimeException("SMS Sending Failed", e);
        }
    }
}