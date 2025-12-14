package org.example.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class StkPushRequest {
    @JsonProperty("BusinessShortCode")
    private String businessShortCode;

    @JsonProperty("Password")
    private String password;

    @JsonProperty("Timestamp")
    private String timestamp;

    @JsonProperty("TransactionType")
    private String transactionType; // "CustomerPayBillOnline"

    @JsonProperty("Amount")
    private String amount;

    @JsonProperty("PartA")
    private String partyA; // User's phone

    @JsonProperty("PartyB")
    private String partyB;

    @JsonProperty("PhoneNumber")
    private String phoneNumber;

    @JsonProperty("CallBackURL")
    private String callBackURL;

    @JsonProperty("AccountReference")
    private String accountReference; // e.g., "Booing-123"

    @JsonProperty("TransactionDesc")
    private String transactionDesc;
}
