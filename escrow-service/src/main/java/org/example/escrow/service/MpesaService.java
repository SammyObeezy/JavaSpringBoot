package org.example.escrow.service;

import org.example.escrow.dto.mpesa.MpesaDto;
import org.example.escrow.model.User;

import java.math.BigDecimal;

public interface MpesaService {

    MpesaDto.StkPushSyncResponse initiateStkPush(User user, BigDecimal amount, String reference);

    void processCallback(MpesaDto.StkCallbackRequest.StkCallback callback);
}
