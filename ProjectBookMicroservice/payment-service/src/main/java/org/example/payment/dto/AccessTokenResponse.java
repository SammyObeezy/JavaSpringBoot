package org.example.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AccessTokenResponse {
    @JsonProperty("access-token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;
}
