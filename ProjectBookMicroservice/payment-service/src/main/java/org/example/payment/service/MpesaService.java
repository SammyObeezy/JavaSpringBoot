package org.example.payment.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.config.MpesaConfig;
import org.example.payment.dto.AccessTokenResponse;
import org.example.payment.dto.MpesaCallbackResponse;
import org.example.payment.dto.StkPushRequest;
import org.example.payment.dto.StkPushResponse;
import org.example.payment.model.Transaction;
import org.example.payment.model.PaymentStatus;
import org.example.payment.repository.TransactionRepository;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final MpesaConfig mpesaConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TransactionRepository transactionRepository;

    /**
     * 1. Get Access Token from Daraja API
     */
    public String getAccessToken() {
        // 1. Prepare Credentials
        String keys = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodeKeys = Base64.getEncoder().encodeToString(keys.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodeKeys);
        headers.set("Cache-Control", "no-cache");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            String url = mpesaConfig.getAuthUrl();
            if (!url.contains("grant_type")) {
                url += "?grant_type=client_credentials";
            }

            log.debug("Sending Auth Request to: {}", url);

            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );

            if (response.getBody() == null) {
                log.error("M-Pesa Auth Failed: Response Body is null");
                return null;
            }

            String token = response.getBody().getAccessToken();

            String maskedToken = token.length() > 5 ? token.substring(0, 5) + "*****" : token;
            log.info("Auth Token Received: {}", maskedToken);

            return token;
        } catch (Exception e) {
            log.error("Error in getAccessToken: {}", e.getMessage());
            return null;
        }
    }
    /**
     * 2. Initiate STK Push
     */
    public void initiateStkPush(Transaction transaction){
        try {
            String token = getAccessToken();
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            // Password = Base64(Shortcode + passkey + Timestamp)
            String passwordStr = mpesaConfig.getShortcode() + mpesaConfig.getPasskey() + timestamp;
            String password = Base64.getEncoder().encodeToString(passwordStr.getBytes(StandardCharsets.UTF_8));

            StkPushRequest request = new StkPushRequest();
            request.setBusinessShortCode(mpesaConfig.getShortcode());
            request.setPassword(password);
            request.setTimestamp(timestamp);
            request.setTransactionType("CustomerPayBillOnline");

            // Amount must be whole number string
            request.setAmount(String.valueOf(transaction.getAmount().intValue()));

            // Format phone: 2547 ...
            // Assuming phone is already cleaned in Auth service, but good to ensure 254 prefix
            String phone = transaction.getPhoneNumber();
            if (phone.startsWith("0")) phone = "254" + phone.substring(1);
            if (phone.startsWith("+")) phone = phone.substring(1);

            request.setPartyA(phone);
            request.setPartyB(mpesaConfig.getShortcode());
            request.setPhoneNumber(phone);
            request.setCallBackURL(mpesaConfig.getCallbackUrl());
            request.setAccountReference("Booking-" + transaction.getBookingId());
            request.setTransactionDesc("Event Booking Payment");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<StkPushRequest> entity = new HttpEntity<>(request, headers);

//            System.out.println("DEBUG: Raw Token: " + token);
//            System.out.println("DEBUG: Auth Header should be: Bearer " + token);

            log.info("Sending STK Push to {}", phone);
            ResponseEntity<StkPushResponse> response = restTemplate.exchange(
                    mpesaConfig.getStkPushUrl(),
                    HttpMethod.POST,
                    entity,
                    StkPushResponse.class
            );

            // 3. Update Transaction with Merchant Request IDS
            StkPushResponse body = response.getBody();
            if (body != null) {
                transaction.setMerchantRequestId(body.getMerchantRequestID());
                transaction.setCheckoutRequestId(body.getCheckoutRequestID());
                transactionRepository.save(transaction);
                log.info("STK Push Initiated Successfully. CheckoutID: {}", body.getCheckoutRequestID());
            }
        } catch (Exception e) {
            log.error("STK Push Failed", e);
            // Don't throw exception here to avoid RabbitMQ infinite retry loops immediately
            // In prod, you might want a Dead Letter Queue strategy
        }
    }
    public void updateTransactionStatus(MpesaCallbackResponse response) {
        MpesaCallbackResponse.StkCallback callback = response.getBody().getStkCallback();
        String checkoutId = callback.getCheckoutRequestID();

        log.info("Processing Callback [CheckoutID: {}]", checkoutId);

        Transaction transaction = transactionRepository.findByCheckoutRequestId(checkoutId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (callback.getResultCode() == 0) {
            log.info("Payment Status: SUCCESS");
            transaction.setStatus(PaymentStatus.COMPLETED);
        } else {
            // Log failure reason clearly
            log.warn("Payment Status: FAILED [Reason: {}]", callback.getResultDesc());
            transaction.setStatus(PaymentStatus.FAILED);
            transaction.setFailureReason(callback.getResultDesc());
        }

        transactionRepository.save(transaction);
        log.info("Transaction updated successfully.");
    }
}