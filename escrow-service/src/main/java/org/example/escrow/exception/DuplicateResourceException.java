package org.example.escrow.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when trying to register an email or phone that already exists.
 * Returns 409 CONFLICT.
 */
public class DuplicateResourceException extends EscrowBaseException {

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s : '%s'", resourceName, fieldName, fieldValue), HttpStatus.CONFLICT);
    }
}