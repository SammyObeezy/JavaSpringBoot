package org.example.escrow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.util.ValidationUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class MpesaClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public MpesaDto.StkPushSyncResponse performStkPush(String phoneNumber, BigDecimal amount, String reference) {
        String token = getAccessToken();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String password = Base64.getEncoder().encodeToString(
                (appProperties.getMpesa().getShortcode() + appProperties.getMpesa().getPasskey() + timestamp).getBytes()
        );
        String sanitizedPhone = ValidationUtils.sanitizePhoneNumber(phoneNumber);

        String jsonPayload = String.format("""
            {
                "BusinessShortCode": "%s",
                "Password": "%s",
                "Timestamp": "%s",
                "TransactionType": "%s",
                "Amount": "%s",
                "PartyA": "%s",
                "PartyB": "%s",
                "PhoneNumber": "%s",
                "CallBackURL": "%s",
                "AccountReference": "%s",
                "TransactionDesc": "Escrow Deposit"
            }
            """, appProperties.getMpesa().getShortcode(), password, timestamp,
                appProperties.getMpesa().getTransactionType(), amount.intValue(),
                sanitizedPhone, appProperties.getMpesa().getShortcode(), sanitizedPhone,
                appProperties.getMpesa().getCallbackUrl(), reference);

        Request request = new Request.Builder()
                .url(appProperties.getMpesa().getStkPushUrl())
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (body == null) throw new BusinessLogicException("Empty response from Safaricom");

            String responseString = body.string();
            if (!response.isSuccessful()) {
                log.error("STK Push Failed: {}", responseString);
                throw new BusinessLogicException("M-Pesa Gateway Error");
            }
            return objectMapper.readValue(responseString, MpesaDto.StkPushSyncResponse.class);
        } catch (IOException e) {
            log.error("Network error during STK Push", e);
            throw new RuntimeException("M-Pesa Network Error", e);
        }
    }

    private String getAccessToken() {
        String credentials = appProperties.getMpesa().getConsumerKey() + ":" + appProperties.getMpesa().getConsumerSecret();
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(appProperties.getMpesa().getAuthUrl())
                .get()
                .addHeader("Authorization", "Basic " + encoded)
                .addHeader("Cache-Control", "no-cache")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                throw new BusinessLogicException("Failed to authenticate with M-Pesa");
            }
            return objectMapper.readValue(body.string(), MpesaDto.AccessTokenResponse.class).getAccessToken();
        } catch (IOException e) {
            log.error("Auth Token Network Error", e);
            throw new RuntimeException("M-Pesa Connection Failed", e);
        }
    }
}