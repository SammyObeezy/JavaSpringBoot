package org.example.escrow.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Parent class for all application-specific exceptions.
 * Allows us to define the HTTP Status code right inside the exception.
 */
@Getter
public abstract class EscrowBaseException extends RuntimeException {

    private final HttpStatus status;

    protected EscrowBaseException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}