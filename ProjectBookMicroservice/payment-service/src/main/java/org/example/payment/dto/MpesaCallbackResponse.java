package org.example.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MpesaCallbackResponse {

    @JsonProperty("Body")
    private Body body;

    @Data
    public static class Body {
        @JsonProperty("stkCallback")
        private StkCallback stkCallback;
    }

    @Data
    public static class StkCallback {
        @JsonProperty("MerchantRequestID")
        private String merchantRequestID;

        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestID;

        @JsonProperty("ResultCode")
        private int resultCode;

        @JsonProperty("ResultDesc")
        private String resultDesc;
    }
}