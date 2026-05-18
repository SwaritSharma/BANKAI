package com.digitalwallet.bnkai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WalletTopupRequest {

    @NotNull(message = "User id is required")
    @com.fasterxml.jackson.annotation.JsonProperty("user_id")
    private Integer userId;

    @NotNull(
            message = "Amount is required"
    )
    @DecimalMin(
            value = "1.0",
            message = "Amount must be greater than 0"
    )
    private BigDecimal amount;

    @NotBlank(message = "Payment method is required")
    @com.fasterxml.jackson.annotation.JsonProperty("payment_method")
    private String paymentMethod;
}