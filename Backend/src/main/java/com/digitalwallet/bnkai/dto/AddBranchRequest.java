package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

@Data
public class AddBranchRequest {
    @NotBlank
    private String street;
    @NotBlank
    private String city;
    @NotBlank
    private String state;
    @NotBlank
    @JsonProperty("postal_code")
    private String postalCode;
    @NotBlank
    private String country;

    @NotNull
    @DecimalMin(value = "0.0")
    @JsonProperty("initial_quantity")
    private BigDecimal initialQuantity;
}
