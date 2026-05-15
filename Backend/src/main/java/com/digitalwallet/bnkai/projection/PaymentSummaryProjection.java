package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PaymentSummaryProjection {

    BigDecimal getAmount();

    String getPaymentMethod();

    String getPaymentStatus();

    String getTransactionType();

    LocalDateTime getCreatedAt();
}