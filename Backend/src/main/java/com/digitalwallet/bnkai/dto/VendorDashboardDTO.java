package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VendorDashboardDTO {
    @JsonProperty("vendor_id")
    private Integer vendorId;

    @JsonProperty("vendor_name")
    private String vendorName;

    private String description;

    @JsonProperty("contact_person_name")
    private String contactPersonName;

    @JsonProperty("contact_email")
    private String contactEmail;

    @JsonProperty("contact_phone")
    private String contactPhone;

    @JsonProperty("website_url")
    private String websiteUrl;

    @JsonProperty("total_gold_quantity")
    private BigDecimal totalGoldQuantity;

    @JsonProperty("current_gold_price")
    private BigDecimal currentGoldPrice;

    @JsonProperty("total_branches")
    private Integer totalBranches;

    @JsonProperty("total_sold_quantity")
    private BigDecimal totalSoldQuantity;
}
