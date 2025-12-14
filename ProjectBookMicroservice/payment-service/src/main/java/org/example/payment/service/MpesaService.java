package org.example.payment.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.payment.config.MpesaConfig;
import org.example.payment.dto.AccessTokenResponse;
import org.example.payment.dto.StkPushRequest;
import org.example.payment.dto.StkPushResponse;
import org.example.payment.model.Transaction;
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
    public String getAccessToken(){
        String keys = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
        String encodeKeys = Base64.getEncoder().encodeToString(keys.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodeKeys);
        headers.set("Cache-Control", "no-code");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                    mpesaConfig.getAuthUrl() + "?grant_type=client_credentials",
                    HttpMethod.GET,
                    entity,
                    AccessTokenResponse.class
            );
            return response.getBody().getAccessToken();
        } catch (Exception e) {
            log.error("Failed to get Access Token", e);
            throw new RuntimeException("M-pesa Auth Failed");
        }
    }

    /**
     * 2. Initiate STK Push
     */
    public void initiateStkPush(Transaction transaction){
        try {
            String token = getAccessToken();
            String timestamp = new SimpleDateFormat("yyyMMddHHmmss").format(new Date());

            // Password = Base64(Shortcode + passkey + Timestamp)
            String passwordStr = mpesaConfig.getShortcode() + mpesaConfig.getPasskey() + timestamp;
            String password = Base64.getEncoder().encodeToString(passwordStr.getBytes());

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
}
