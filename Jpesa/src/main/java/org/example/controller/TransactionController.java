package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import org.example.dto.SendMoneyRequest;
import org.example.dto.TransactionRequest;
import org.example.model.Transaction;
import org.example.repository.TransactionRepository;
import org.example.repository.UserRepository;
import org.example.repository.WalletRepository;
import org.example.service.TransactionService;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TransactionController {

    private final TransactionService transactionService;
    private final ObjectMapper objectMapper;

    // Define the Key to retrieve the attached phone number from AuthMiddleware
    public static final AttachmentKey<String> USER_PHONE_KEY = AttachmentKey.create(String.class);

    public TransactionController() {
        this.transactionService = new TransactionService(
                new UserRepository(),
                new WalletRepository(),
                new TransactionRepository()
        );
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    // --- HANDLER DEFINITIONS ---
    public HttpHandler depositHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleDeposit);
                return;
            }
            handleDeposit(exchange);
        };
    }

    public HttpHandler airtimeHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleAirtime);
                return;
            }
            handleAirtime(exchange);
        };
    }

    public HttpHandler sendMoneyHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleSendMoney);
                return;
            }
            handleSendMoney(exchange);
        };
    }

    public HttpHandler miniStatementHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleMiniStatement);
                return;
            }
            handleMiniStatement(exchange);
        };
    }

    // --- LOGIC ---

    private void handleDeposit(HttpServerExchange exchange) {
        try {
            exchange.startBlocking();
            String authenticatedPhone = exchange.getAttachment(USER_PHONE_KEY);

            InputStream inputStream = exchange.getInputStream();
            TransactionRequest request = objectMapper.readValue(inputStream, TransactionRequest.class);

            // Force security: Use the phone number from the Token
            request.setPhoneNumber(authenticatedPhone);

            Transaction txn = transactionService.deposit(request);
            sendSuccess(exchange, txn);

        } catch (Exception e) {
            handleError(exchange, e);
        }
    }

    private void handleAirtime(HttpServerExchange exchange) {
        try {
            exchange.startBlocking();
            String authenticatedPhone = exchange.getAttachment(USER_PHONE_KEY);

            InputStream inputStream = exchange.getInputStream();
            TransactionRequest request = objectMapper.readValue(inputStream, TransactionRequest.class);

            request.setPhoneNumber(authenticatedPhone);

            Transaction txn = transactionService.buyAirtime(request);
            sendSuccess(exchange, txn);

        } catch (Exception e) {
            handleError(exchange, e);
        }
    }

    private void handleSendMoney(HttpServerExchange exchange) {
        try {
            exchange.startBlocking();
            String authenticatedPhone = exchange.getAttachment(USER_PHONE_KEY);

            InputStream inputStream = exchange.getInputStream();
            SendMoneyRequest request = objectMapper.readValue(inputStream, SendMoneyRequest.class);

            // Force Sender to be the logged-in user
            request.setSenderPhone(authenticatedPhone);

            transactionService.sendMoney(request);

            String jsonResponse = objectMapper.writeValueAsString(Map.of("message", "Transfer Successful"));
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(jsonResponse);

        } catch (Exception e) {
            handleError(exchange, e);
        }
    }

    private void handleMiniStatement(HttpServerExchange exchange) {
        try {
            // SECURE UPDATE: We no longer need ?phoneNumber=... in the URL.
            // We get the phone number directly from the Token.
            String authenticatedPhone = exchange.getAttachment(USER_PHONE_KEY);

            List<Transaction> statement = transactionService.getMiniStatement(authenticatedPhone);

            String jsonResponse = objectMapper.writeValueAsString(statement);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(jsonResponse);

        } catch (Exception e) {
            handleError(exchange, e);
        }
    }

    // --- HELPERS ---

    private void sendSuccess(HttpServerExchange exchange, Object body) throws Exception {
        String jsonResponse = objectMapper.writeValueAsString(Map.of(
                "message", "Success",
                "receipt", body
        ));
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.setStatusCode(200);
        exchange.getResponseSender().send(jsonResponse);
    }

    private void handleError(HttpServerExchange exchange, Exception e) {
        e.printStackTrace();
        exchange.setStatusCode(500);
        if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) {
            exchange.setStatusCode(400); // Bad Request
        }
        exchange.getResponseSender().send("{\"error\": \"" + e.getMessage() + "\"}");
    }
}