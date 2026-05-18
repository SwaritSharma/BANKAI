package com.digitalwallet.bnkai.exception;

public class UnauthorizedHoldingAccessException
        extends RuntimeException {

    public UnauthorizedHoldingAccessException(
            String message
    ) {

        super(message);
    }
}