package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;

public interface VendorDashboardProjection {

    String getVendorName();

    BigDecimal getCurrentGoldPrice();

    BigDecimal getTotalGoldQuantity();

    String getContactPersonName();

    String getContactEmail();

    String getContactPhone();

    String getWebsiteUrl();
}