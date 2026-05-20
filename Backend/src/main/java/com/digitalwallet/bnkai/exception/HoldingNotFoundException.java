package com.digitalwallet.bnkai.exception;

public class HoldingNotFoundException
        extends RuntimeException {

    public HoldingNotFoundException(
            String message
    ) {

        super(message);
    }
}