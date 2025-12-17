package org.example.payment.controller; // or your package name

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/mobile-money")
public class PaymentController {

    // Safaricom hits this URL: /mobile-money/callback
    @PostMapping("/callback")
    public void processMpesaCallback(@RequestBody Map<String, Object> payload) {

        // 1. Log the JSON so we can see the payment details!
        System.out.println("----------- MPESA CALLBACK RECEIVED -----------");
        System.out.println(payload);
        System.out.println("-----------------------------------------------");

        // TODO: Later we will extract the ResultCode and update the Database
    }
}