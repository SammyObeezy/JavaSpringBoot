package org.example;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.util.Headers;
import org.example.config.AuthMiddleware;
import org.example.config.DatabaseConfig;
import org.example.controller.AuthController;
import org.example.controller.TransactionController;

public class JpesaApp {

    public static void main(String[] args) {
        // 1. Initialize Controllers
        AuthController authController = new AuthController();
        TransactionController txnController = new TransactionController();

        // 2. Define Routes (The "Traffic Cop")
        RoutingHandler routes = new RoutingHandler()
                // Health Check
                .get("/health", exchange -> {
                    exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                    exchange.getResponseSender().send("Jpesa System Online");
                })

                // Auth Routes
                .post("/api/auth/register", authController.registerHandler())
                .post("/api/auth/login", authController.loginHandler())
                .post("/api/auth/verify-otp", authController.verifyOtpHandler())

                // Transactions (New!)
                .post("/api/txn/deposit", new AuthMiddleware(txnController.depositHandler()))
                .post("/api/txn/airtime",  new AuthMiddleware(txnController.airtimeHandler()))
                .post("/api/txn/send",  new AuthMiddleware(txnController.sendMoneyHandler()))
                .get("/api/txn/ministatement",  new AuthMiddleware(txnController.miniStatementHandler()));


        // 3. Configure & Start Server
        int port = 8080;
        String host = "0.0.0.0"; // Listens on all interfaces

        Undertow server = Undertow.builder()
                .addHttpListener(port, host)
                .setHandler(routes)
                .build();

        System.out.println("------------------------------------------------");
        System.out.println(" J-PESA BACKEND STARTED");
        System.out.println(" Database: " + "Connected (HikariCP)");
        System.out.println(" Server:   http://" + host + ":" + port);
        System.out.println("------------------------------------------------");

        server.start();

        // 4. Add Shutdown Hook to close DB pool gracefully
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Stopping J-PESA...");
            DatabaseConfig.close();
            server.stop();
        }));
    }
}