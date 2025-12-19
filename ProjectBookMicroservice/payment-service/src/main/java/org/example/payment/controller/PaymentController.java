package org.example.payment.controller; // Make sure this matches your Main App package

import lombok.extern.slf4j.Slf4j;
import org.example.payment.dto.MpesaCallbackResponse;
import org.example.payment.service.MpesaService;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/mobile-money")
public class PaymentController {

    private final MpesaService mpesaService;

    public PaymentController(MpesaService mpesaService) {
        this.mpesaService = mpesaService;
    }

    @PostMapping("/callback")
    public void processCallback(@RequestBody MpesaCallbackResponse payload) {
        log.info("Received M-Pesa Callback payload.");
        mpesaService.updateTransactionStatus(payload);
    }
}