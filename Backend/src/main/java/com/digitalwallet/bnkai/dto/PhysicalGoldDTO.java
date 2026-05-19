package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PhysicalGoldDTO {
    @JsonProperty("transaction_id")
    private Integer physicalTransactionId;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    
    @JsonProperty("delivery_address")
    private HoldingDTO.AddressDTO deliveryAddress;
}
