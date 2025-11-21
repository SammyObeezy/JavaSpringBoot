package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.model.User;
import org.example.service.UserService;

import java.io.IOException;
import java.io.InputStream;

public class AuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public AuthController() {
        this.userService = new UserService();
        this.objectMapper = new ObjectMapper();
        // Critical: Register module to handle LocalDateTime in JSON
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * Handler for POST /api/auth/register
     */
    public HttpHandler registerHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleRegister); // Offload to worker thread
                return;
            }
            handleRegister(exchange);
        };
    }

    private void handleRegister(HttpServerExchange exchange) {
        try {
            // FIX: Must explicitly switch to blocking mode before reading the stream
            exchange.startBlocking();

            // 1. Read JSON from request body (InputStream) -> DTO
            InputStream inputStream = exchange.getInputStream();
            RegisterRequest request = objectMapper.readValue(inputStream, RegisterRequest.class);

            // 2. Call Service Logic
            User newUser = userService.registerUser(request);

            // 3. Send Response (Convert Model -> JSON)
            String jsonResponse = objectMapper.writeValueAsString(newUser);

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(201); // Created
            exchange.getResponseSender().send(jsonResponse);

        } catch (IllegalArgumentException e) {
            // Client Error (Bad Input)
            exchange.setStatusCode(400);
            exchange.getResponseSender().send("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            // Server Error
            e.printStackTrace();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\": \"Internal Server Error\"}");
        }
    }

    /**
     * Handler for POST /api/auth/login
     */
    public HttpHandler loginHandler() {
        return exchange -> {
            if (exchange.isInIoThread()) {
                exchange.dispatch(this::handleLogin);
                return;
            }
            handleLogin(exchange);
        };
    }

    private void handleLogin(HttpServerExchange exchange) {
        try {
            // FIX: Must explicitly switch to blocking mode here too
            exchange.startBlocking();

            InputStream inputStream = exchange.getInputStream();
            LoginRequest request = objectMapper.readValue(inputStream, LoginRequest.class);

            System.out.println("Login attempt for: " + request.getPhoneNumber());

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send("{\"message\": \"Login logic coming soon!\"}");

        } catch (Exception e) {
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\": \"Login Failed\"}");
        }
    }
}