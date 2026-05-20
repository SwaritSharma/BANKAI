package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HoldingDTO {
    @JsonProperty("holding_id")
    private Integer holdingId;

    @JsonProperty("vendor_name")
    private String vendorName;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("currentValue")
    private BigDecimal currentValue;

    @JsonProperty("current_gold_price")
    private BigDecimal currentGoldPrice;

    @JsonProperty("value")
    private BigDecimal value;

    @JsonProperty("branch_address")
    private AddressDTO branchAddress;

    @Data
    public static class AddressDTO {
        @JsonProperty("address_id")
        private Integer addressId;
        private String street;
        private String city;
        private String state;
        @JsonProperty("postal_code")
        private String postalCode;
        private String country;
    }
}
