package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.entity.Payment;
import com.digitalwallet.bnkai.entity.User;

import java.math.BigDecimal;

public interface PaymentService {

    Payment createWalletCreditEntry(
            User user,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    );

    Payment createWalletDebitEntry(
            User user,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    );
}