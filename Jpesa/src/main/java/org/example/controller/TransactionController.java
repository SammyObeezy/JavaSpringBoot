package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.example.dto.SendMoneyRequest;
import org.example.dto.TransactionRequest;
import org.example.model.Transaction;
import org.example.service.TransactionService;

import java.io.InputStream;
import java.util.Deque;
import java.util.List;
import java.util.Map;

public class TransactionController {

    private final TransactionService transactionService;
    private  final ObjectMapper objectMapper;

    public TransactionController(){
        this.transactionService = new TransactionService();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public HttpHandler depositHandler() {
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleDeposit);
                return;
            }
            handleDeposit(exchange);
        };
    }

    public HttpHandler airtimeHandler() {
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleAirtime);
                return;
            }
            handleAirtime(exchange);
        };
    }

    public HttpHandler miniStatementHandler() {
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleMiniStatement);
                return;
            }
            handleMiniStatement(exchange);
        };
    }

    public HttpHandler sendMoneyHandler() {
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleSendMoney);
                return;
            }
            handleSendMoney(exchange);
        };
    }

    private void handleSendMoney(HttpServerExchange exchange){
        try {
            exchange.startBlocking();
            InputStream inputStream = exchange.getInputStream();
            SendMoneyRequest request = objectMapper.readValue(inputStream, SendMoneyRequest.class);

            transactionService.sendMoney(request);

            /// Send simple success message
            String jsonResponse = objectMapper.writeValueAsString(Map.of("message", "Transfer Successful"));
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(jsonResponse);

        } catch (Exception e) {
            handleError(exchange, e);
        }
    }

    private void handleDeposit(HttpServerExchange exchange){
        try {
            exchange.startBlocking();
            InputStream inputStream  =exchange.getInputStream();
            TransactionRequest request = objectMapper.readValue(inputStream, TransactionRequest.class);

            Transaction txn = transactionService.deposit(request);
            sendSuccess(exchange, txn);
        } catch (Exception e){
            handleError(exchange, e);
        }
    }

    private void handleAirtime(HttpServerExchange exchange){
        try {
            exchange.startBlocking();
            InputStream inputStream = exchange.getInputStream();
            TransactionRequest request = objectMapper.readValue(inputStream, TransactionRequest.class);

            Transaction txn = transactionService.buyAirtime(request);
            sendSuccess(exchange, txn);
        } catch (Exception e){
            handleError(exchange, e);
        }
    }

    private void handleMiniStatement(HttpServerExchange exchange){
        try{
            // Get Query Param: /api/txn/ministatement?phoneNumber=07...
            Map<String, Deque<String>> params = exchange.getQueryParameters();
            if (!params.containsKey("phoneNumber")) {
                throw new IllegalArgumentException("phoneNumber query is required");
            }

            String phoneNumber = params.get("phoneNumber").getFirst();
            List<Transaction> statement = transactionService.getMiniStatement(phoneNumber);

            String jsonResponse = objectMapper.writeValueAsString(statement);
            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(jsonResponse);
        } catch (Exception e){
            handleError(exchange, e);
        }
    }
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
