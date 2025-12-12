//package org.example.auth.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import software.amazon.awssdk.services.sns.SnsClient;
//import software.amazon.awssdk.services.sns.model.PublishRequest;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class AwsNotificationService implements NotificationService {
//    private static final Logger log = LoggerFactory.getLogger(AwsNotificationService.class);
//
//    // You need to configure AWS credentials in your environment or application.properties
//    // For local dev without AWS keys, you might want to mark this @Primary only if a profile is active
//
//    // Lazy injection or manual bean creation is preferred if keys might be missing
//    // For this code to run, we assume SnsClient is configured as a Bean or we create it here.
//    private final SnsClient snsClient;
//
//    @Override
//    public void sendSms(String phoneNumber, String message) {
//        log.info("Sending AWS SMS to {}", phoneNumber, message);
//        try {
//            PublishRequest request = PublishRequest.builder()
//                    .phoneNumber(phoneNumber)
//                    .message(message)
//                    .build();
//
//            snsClient.publish(request);
//            log.info("SMS sent successfully.");
//        } catch (Exception e) {
//            log.error("AWS SNS Error: {}", e.getMessage());
//        }
//        throw new RuntimeException("Failed to send OTP via SMS");
//    }
//
//    @Override
//    public void sendEmail(String toEmail, String subject, String body) {
//        // Implement SES logic similarly using SesClient
//        log.info("Sending AWS Email (Mock) to {}", toEmail);
//    }
//}