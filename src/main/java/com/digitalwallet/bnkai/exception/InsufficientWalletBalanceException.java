package com.digitalwallet.bnkai.exception;

public class InsufficientWalletBalanceException
        extends RuntimeException {

    public InsufficientWalletBalanceException(
            String message
    ) {

        super(message);
    }
}