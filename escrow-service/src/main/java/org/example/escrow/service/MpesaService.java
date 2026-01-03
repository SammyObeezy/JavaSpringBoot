package org.example.escrow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.example.escrow.config.AppProperties;
import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.dto.wallet.DepositRequest;
import org.example.escrow.exception.BusinessLogicException;
import org.example.escrow.exception.ResourceNotFoundException;
import org.example.escrow.model.MpesaTransaction;
import org.example.escrow.model.User;
import org.example.escrow.repository.MpesaTransactionRepository;
import org.example.escrow.util.ValidationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final MpesaTransactionRepository mpesaTransactionRepository;
    private final WalletServiceImpl walletService; // To credit funds

    /**
     * 1. Get Access Token
     */
    public String getAccessToken() {
        String consumerKey = appProperties.getMpesa().getConsumerKey();
        String consumerSecret = appProperties.getMpesa().getConsumerSecret();
        String credentials = consumerKey + ":" + consumerSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        Request request = new Request.Builder()
                .url(appProperties.getMpesa().getAuthUrl())
                .get()
                .addHeader("Authorization", "Basic " + encodedCredentials)
                .addHeader("Cache-Control", "no-cache")
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new BusinessLogicException("Failed to authenticate with M-Pesa");
            }
            MpesaDto.AccessTokenResponse tokenResponse = objectMapper.readValue(response.body().string(), MpesaDto.AccessTokenResponse.class);
            return tokenResponse.getAccessToken();
        } catch (IOException e) {
            log.error("M-Pesa Auth Error", e);
            throw new RuntimeException("M-Pesa Connection Failed");
        }
    }

    /**
     * 2. Trigger STK Push & Save Pending Transaction
     */
    @Transactional
    public MpesaDto.StkPushSyncResponse initiateStkPush(User user, BigDecimal amount, String reference) {
        String token = getAccessToken();
        String shortcode = appProperties.getMpesa().getShortcode();
        String passkey = appProperties.getMpesa().getPasskey();
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String password = Base64.getEncoder().encodeToString((shortcode + passkey + timestamp).getBytes());
        String sanitizedPhone = ValidationUtils.sanitizePhoneNumber(user.getPhoneNumber());

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
            """, shortcode, password, timestamp, appProperties.getMpesa().getTransactionType(),
                amount.intValue(), sanitizedPhone, shortcode, sanitizedPhone,
                appProperties.getMpesa().getCallbackUrl(), reference);

        RequestBody body = RequestBody.create(jsonPayload, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(appProperties.getMpesa().getStkPushUrl())
                .post(body)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            String responseBody = response.body().string();

            if (!response.isSuccessful()) {
                log.error("M-Pesa STK Push Failed: {}", responseBody);
                throw new BusinessLogicException("M-Pesa STK Push Failed.");
            }

            MpesaDto.StkPushSyncResponse stkResponse = objectMapper.readValue(responseBody, MpesaDto.StkPushSyncResponse.class);

            // SAVE PENDING TRANSACTION
            MpesaTransaction transaction = MpesaTransaction.builder()
                    .user(user)
                    .amount(amount)
                    .merchantRequestId(stkResponse.getMerchantRequestId())
                    .checkoutRequestId(stkResponse.getCheckoutRequestId())
                    .status("PENDING")
                    .build();

            mpesaTransactionRepository.save(transaction);

            return stkResponse;
        } catch (IOException e) {
            log.error("STK Push IO Error", e);
            throw new RuntimeException("Failed to initiate STK Push");
        }
    }

    /**
     * 3. Process Callback
     */
    @Transactional
    public void processCallback(MpesaDto.StkCallbackRequest.StkCallback callbackBody) {
        String checkoutRequestId = callbackBody.getCheckoutRequestId();

        // 1. Find the pending transaction
        MpesaTransaction transaction = mpesaTransactionRepository.findByCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new ResourceNotFoundException("MpesaTransaction", "checkoutRequestId", checkoutRequestId));

        if (!"PENDING".equals(transaction.getStatus())) {
            log.warn("Duplicate Callback ignored for ID: {}", checkoutRequestId);
            return;
        }

        // 2. Check Success
        if (callbackBody.getResultCode() == 0) {
            transaction.setStatus("COMPLETED");

            // Extract Receipt Number
            List<MpesaDto.StkCallbackRequest.Item> items = callbackBody.getCallbackMetadata().getItem();
            String receipt = items.stream()
                    .filter(item -> "MpesaReceiptNumber".equals(item.getName()))
                    .findFirst()
                    .map(item -> item.getValue().toString())
                    .orElse("UNKNOWN");

            transaction.setMpesaReceiptNumber(receipt);
            mpesaTransactionRepository.save(transaction);

            // 3. CREDIT WALLET
            // We use a DTO to call the existing wallet service logic
            DepositRequest deposit = new DepositRequest();
            deposit.setAmount(transaction.getAmount());
            deposit.setCurrency("KES"); // Default currency

            walletService.depositFunds(transaction.getUser().getId(), deposit);

            log.info("Wallet Credited for User: {}, Amount: {}, Receipt: {}",
                    transaction.getUser().getEmail(), transaction.getAmount(), receipt);

        } else {
            // Handle Failure
            transaction.setStatus("FAILED");
            transaction.setFailureReason(callbackBody.getResultDesc());
            mpesaTransactionRepository.save(transaction);
            log.warn("M-Pesa Transaction Failed: {}", callbackBody.getResultDesc());
        }
    }
}