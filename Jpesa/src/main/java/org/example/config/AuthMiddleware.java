package org.example.config;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HeaderMap;
import org.example.controller.TransactionController; // Import the Controller
import org.example.util.JwtUtil;

public class AuthMiddleware implements HttpHandler {

    private final HttpHandler next;

    public AuthMiddleware(HttpHandler next) {
        this.next = next;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        HeaderMap headers = exchange.getRequestHeaders();
        String authHeader = headers.getFirst(Headers.AUTHORIZATION);

        // 1. Check if Header exists
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(exchange, "Missing or Invalid Authorization Header");
            return;
        }

        // 2. Extract Token
        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // 3. Validate Token
            String phoneNumber = JwtUtil.validateTokenAndGetPhone(token);

            // 4. Attach the phone number using the SHARED KEY from TransactionController
            // FIX: Do not use 'AttachmentKey.create()' here, use the existing static key.
            exchange.putAttachment(TransactionController.USER_PHONE_KEY, phoneNumber);

            // 5. Allow request to proceed to the Controller
            next.handleRequest(exchange);

        } catch (RuntimeException e) {
            sendUnauthorized(exchange, "Invalid Token: " + e.getMessage());
        }
    }

    private void sendUnauthorized(HttpServerExchange exchange, String message) {
        exchange.setStatusCode(401);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send("{\"error\": \"" + message + "\"}");
    }
}