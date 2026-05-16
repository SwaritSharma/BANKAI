package com.digitalwallet.bnkai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BranchAllocationResponse {

    private String vendorName;

    private BigDecimal availableQuantity;

    private String street;

    private String city;

    private String state;

    private String postalCode;

    private String country;
}