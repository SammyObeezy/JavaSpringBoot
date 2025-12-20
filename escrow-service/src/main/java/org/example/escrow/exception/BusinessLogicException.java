package org.example.escrow.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a business rule is violated (e.g., Insufficient Funds, Invalid State Transition).
 * Returns 400 BAD REQUEST.
 */
public class BusinessLogicException extends EscrowBaseException {

    public BusinessLogicException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}