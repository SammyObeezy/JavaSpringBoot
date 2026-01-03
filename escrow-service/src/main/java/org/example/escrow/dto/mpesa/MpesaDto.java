package org.example.escrow.dto.mpesa;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class MpesaDto {

    // 1. Response when we ask for a Token
    @Data
    public static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("expires_in")
        private String expiresIn;
    }

    // 2. Response immediately after triggering STK Push
    @Data
    public static class StkPushSyncResponse {
        @JsonProperty("MerchantRequestID")
        private String merchantRequestId;
        @JsonProperty("CheckoutRequestID")
        private String checkoutRequestId;
        @JsonProperty("ResponseCode")
        private String responseCode;
        @JsonProperty("ResponseDescription")
        private String responseDescription;
        @JsonProperty("CustomerMessage")
        private String customerMessage;
    }

    // 3. The Callback (Webhook) from Safaricom
    @Data
    public static class StkCallbackRequest {
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
            private String merchantRequestId;
            @JsonProperty("CheckoutRequestID")
            private String checkoutRequestId;
            @JsonProperty("ResultCode")
            private Integer resultCode;
            @JsonProperty("ResultDesc")
            private String resultDesc;
            @JsonProperty("CallbackMetadata")
            private CallbackMetadata callbackMetadata;
        }

        @Data
        public static class CallbackMetadata {
            @JsonProperty("Item")
            private java.util.List<Item> item;
        }

        @Data
        public static class Item {
            @JsonProperty("Name")
            private String name;
            @JsonProperty("Value")
            private Object value;
        }
    }
}