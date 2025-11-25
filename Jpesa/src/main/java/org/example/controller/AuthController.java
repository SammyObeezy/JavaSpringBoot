package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import org.example.dto.*;
import org.example.model.User;
import org.example.service.UserService;
import org.example.util.JwtUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
            UserResponse response = new UserResponse(newUser);
            String jsonResponse = objectMapper.writeValueAsString(response);

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

            // Logic: Verify credentials & Generate OTP
            String message = userService.loginUser(request);

            // Send JSON response
            // Map.of is a Java 9+ shortcut to create a quick map for JSON
            String jsonResponse = objectMapper.writeValueAsString(Map.of("message", message));

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200); //OK
            exchange.getResponseSender().send(jsonResponse);

        } catch (Exception e) {
            e.printStackTrace();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\": \"Login Failed\"}");
        }
    }
    // Verify OTP
    public HttpHandler verifyOtpHandler(){
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleVerifyOtp);
                return;
            }
            handleVerifyOtp(exchange);
        };
    }
    private void handleVerifyOtp(HttpServerExchange exchange){
        try {
            exchange.startBlocking();
            InputStream inputStream = exchange.getInputStream();
            VerifyOtpRequest request = objectMapper.readValue(inputStream, VerifyOtpRequest.class);

            User user = userService.verifyOtp(request);

            // NEW: Generate Token
            String token = JwtUtil.generateToken(user.getPhoneNumber(), user.getUserId());

            UserResponse response = new UserResponse(user); // Hide password

            // Return success
            String jsonResponse = objectMapper.writeValueAsString(Map.of(
                    "message", "Authentication Successful",
                    "token", token,
                    "user", response
            ));

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send(jsonResponse);
        } catch (IllegalArgumentException e) {
            exchange.setStatusCode(401); // Unauthorized / Bad Request
            exchange.getResponseSender().send("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            exchange.setStatusCode(500);
            exchange.getResponseSender().send("{\"error\": \"Verification Failed\"}");
        }
    }
    /**
     * Handler for Reset Password
     */
    public HttpHandler initiateResetHandler() {
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleInitiateReset);
                return;
            }
            handleInitiateReset(exchange);
        };
    }
    public HttpHandler completeResetHandler(){
        return exchange -> {
            if (exchange.isInIoThread()){
                exchange.dispatch(this::handleCompleteReset);
                return;
            }
            handleCompleteReset(exchange);
        };
    }

    private void handleInitiateReset(HttpServerExchange exchange){
        try {
            exchange.startBlocking();
            // Expecting simple JSON "{"phoneNumber" : "..."}
            Map<String, String> body = objectMapper.readValue(exchange.getInputStream(), Map.class);
            String phone = body.get("phoneNumber");

            String msg = userService.initiatePasswordReset(phone);

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send("{\"message\": \"" + msg + "\"}");
        } catch (Exception e){
            exchange.setStatusCode(400);
            exchange.getResponseSender().send("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    private void handleCompleteReset(HttpServerExchange exchange) {
        try {
            exchange.startBlocking();
            PasswordResetRequest request = objectMapper.readValue(exchange.getInputStream(), PasswordResetRequest.class);

            userService.completePasswordReset(request);

            exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
            exchange.setStatusCode(200);
            exchange.getResponseSender().send("{\"message\": \"Password reset successful\"}");

        } catch (Exception e) {
            exchange.setStatusCode(400);
            exchange.getResponseSender().send("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}