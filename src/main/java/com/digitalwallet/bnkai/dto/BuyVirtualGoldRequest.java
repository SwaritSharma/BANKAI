package com.digitalwallet.bnkai.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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
public class BuyVirtualGoldRequest {

    @NotNull(message = "User id is required")
    @com.fasterxml.jackson.annotation.JsonProperty("user_id")
    private Integer userId;

    @NotNull(message = "Vendor id is required")
    @com.fasterxml.jackson.annotation.JsonProperty("vendor_id")
    private Integer vendorId;

    @NotNull(
            message = "Quantity is required"
    )
    @DecimalMin(
            value = "0.00001",
            message = "Quantity must be greater than 0"
    )
    @Digits(integer = 10, fraction = 8, message = "Quantity can have up to 8 decimal places")
    private BigDecimal quantity;
}