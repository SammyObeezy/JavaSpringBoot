package org.example.escrow.service;

import org.example.escrow.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AwsSnsServiceTest {

    @Mock private SnsClient snsClient;
    @Mock private AppProperties appProperties;
    @Mock private AppProperties.Aws awsProps;

    @InjectMocks
    private AwsSnsService awsSnsService;

    @BeforeEach
    void setUp() {
        // Mock the nested config structure: appProperties.getAws().getSnsSenderId()
        lenient().when(appProperties.getAws()).thenReturn(awsProps);
        lenient().when(awsProps.getSnsSenderId()).thenReturn("EscrowApp");
    }

    @Test
    void sendSms_ShouldPublishToSns_WithCorrectAttributes() {
        // Arrange
        String phoneNumber = "254712345678"; // Already sanitized format, but missing +
        String message = "Your OTP is 123456";
        PublishResponse mockResponse = PublishResponse.builder().messageId("msg-123").build();

        when(snsClient.publish(any(PublishRequest.class))).thenReturn(mockResponse);

        // Act
        awsSnsService.sendSms(phoneNumber, message);

        // Assert
        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient).publish(captor.capture());

        PublishRequest request = captor.getValue();

        // Verify Phone Number Formatting (+ prefix added logic)
        assertEquals("+254712345678", request.phoneNumber());
        assertEquals(message, request.message());

        // Verify Message Attributes (Sender ID and Type are critical for delivery)
        Map<String, MessageAttributeValue> attrs = request.messageAttributes();
        assertEquals("EscrowApp", attrs.get("AWS.SNS.SMS.SenderID").stringValue());
        assertEquals("Transactional", attrs.get("AWS.SNS.SMS.SMSType").stringValue());
    }

    @Test
    void sendSms_ShouldCatchException_AndLogFallback_WhenSnsFails() {
        // This tests the "Dev Mode" fallback we implemented
        // Arrange
        String phoneNumber = "254712345678";
        String message = "Test Message";

        // Simulate AWS failure (e.g. Invalid Credentials)
        when(snsClient.publish(any(PublishRequest.class)))
                .thenThrow(SnsException.builder().message("The security token included in the request is invalid").build());

        // Act & Assert
        // The service catches the exception and logs it, so no exception should be thrown here.
        // If this throws, our fallback logic is broken.
        assertDoesNotThrow(() -> awsSnsService.sendSms(phoneNumber, message));

        verify(snsClient).publish(any(PublishRequest.class));
    }
}