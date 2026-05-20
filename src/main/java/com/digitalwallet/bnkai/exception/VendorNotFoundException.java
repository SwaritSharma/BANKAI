package com.digitalwallet.bnkai.exception;

public class VendorNotFoundException
        extends RuntimeException {

    public VendorNotFoundException(
            String message
    ) {

        super(message);
    }
}