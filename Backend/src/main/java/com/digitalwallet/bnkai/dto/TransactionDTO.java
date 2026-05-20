package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDTO {
    @JsonProperty("transaction_id")
    private Integer transactionId;

    @JsonProperty("transaction_type")
    private String transactionType;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("user_address")
    private String userAddress;

    @JsonProperty("branch_name")
    private String branchName;

    @JsonProperty("branch_address")
    private String branchAddress;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("transaction_status")
    private String transactionStatus;
}
