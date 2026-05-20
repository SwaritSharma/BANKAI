package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentDTO {
    @JsonProperty("payment_id")
    private Integer paymentId;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("payment_method")
    private String paymentMethod;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("payment_status")
    private String paymentStatus;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
