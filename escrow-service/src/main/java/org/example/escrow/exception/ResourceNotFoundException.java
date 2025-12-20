package org.example.escrow.exception;

import org.springframework.http.HttpStatus;

/**
 * Thrown when looking up an ID (User, Wallet, Order) that doesn't exist.
 * Returns 404 NOT FOUND.
 */
public class ResourceNotFoundException extends EscrowBaseException {

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), HttpStatus.NOT_FOUND);
    }
}