package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VendorDTO {
    @JsonProperty("vendor_id")
    private Integer vendorId;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("current_gold_price")
    private BigDecimal currentGoldPrice;
}
