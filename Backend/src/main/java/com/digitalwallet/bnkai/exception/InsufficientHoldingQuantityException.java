package com.digitalwallet.bnkai.exception;

public class InsufficientHoldingQuantityException
        extends RuntimeException {

    public InsufficientHoldingQuantityException(
            String message
    ) {

        super(message);
    }
}