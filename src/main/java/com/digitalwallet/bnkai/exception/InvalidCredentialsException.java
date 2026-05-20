package com.digitalwallet.bnkai.exception;

public class InvalidCredentialsException
        extends RuntimeException {

    public InvalidCredentialsException(
            String message
    ) {

        super(message);
    }
}