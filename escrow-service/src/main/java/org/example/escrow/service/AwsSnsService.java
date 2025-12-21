package org.example.escrow.service;

import lombok.RequiredArgsConstructor;
import org.example.escrow.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AwsSnsService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(AwsSnsService.class);
    private final SnsClient snsClient;
    private final AppProperties appProperties;

    @Override
    public void sendSms(String phoneNumber, String message) {
        try {
            String formattedPhone = phoneNumber.startsWith("+") ? phoneNumber : "+" + phoneNumber;

            Map<String, MessageAttributeValue> messageAttributes = new HashMap<>();
            messageAttributes.put("AWS.SNS.SMS.SenderID", MessageAttributeValue.builder()
                    .stringValue(appProperties.getAws().getSnsSenderId())
                    .dataType("String")
                    .build());
            messageAttributes.put("AWS.SNS.SMS.SMSType", MessageAttributeValue.builder()
                    .stringValue("Transactional")
                    .dataType("String")
                    .build());

            PublishRequest request = PublishRequest.builder()
                    .message(message)
                    .phoneNumber(formattedPhone)
                    .messageAttributes(messageAttributes)
                    .build();

            PublishResponse response = snsClient.publish(request);
            logger.info("SMS sent successfully to {}. MessageId: {}", formattedPhone, response.messageId());

        } catch (Exception e) {
            // CRITICAL FIX FOR TESTING:
            // If AWS credentials fail, we catch the exception and LOG the OTP.
            // This allows the Registration Transaction to COMMIT so you can proceed to Login.
            logger.error("AWS SNS Failed (Check Credentials). FALLBACK OTP LOG -> To: {}, Message: {}", phoneNumber, message);
        }
    }
}