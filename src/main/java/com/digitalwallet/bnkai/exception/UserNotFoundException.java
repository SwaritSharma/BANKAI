package com.digitalwallet.bnkai.exception;

public class UserNotFoundException
        extends RuntimeException {

    public UserNotFoundException(
            String message
    ) {

        super(message);
    }
}