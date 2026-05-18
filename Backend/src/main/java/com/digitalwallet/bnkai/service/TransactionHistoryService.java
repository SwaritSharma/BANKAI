package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VendorBranch;

import java.math.BigDecimal;

public interface TransactionHistoryService {

    TransactionHistory createBuyTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    );

    TransactionHistory createSellTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    );

    TransactionHistory
    createConvertToPhysicalTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    );
}