package org.example.escrow.exception;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    // Optional: For validation errors (e.g., "email": "must be valid")
    private Map<String, String> validationErrors;
}
