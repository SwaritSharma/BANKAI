package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionSummaryProjection {

    String getTransactionType();

    String getTransactionStatus();

    BigDecimal getAmount();

    BigDecimal getQuantity();

    String getVendorName();

    AddressProjection getBranchAddress();

    LocalDateTime getCreatedAt();
}