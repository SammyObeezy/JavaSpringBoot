package org.example.escrow.dto.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Generic Wrapper for all API responses.
 * Ensures consistent JSON structure across the entire application.
 * @param <T> The type of the data being returned.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    @Builder.Default
    private boolean success = true;

    private String message;

    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Static Factory methods for cleaner code in Controllers
    public static <T> ApiResponse<T> success(T data, String message){
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    // Suppress unused warning as this is a utility method for future use
    @SuppressWarnings("unused")
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}