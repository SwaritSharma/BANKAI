package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;

public interface HoldingSummaryProjection {

    String getVendorName();

    BigDecimal getQuantity();

    BigDecimal getCurrentGoldPrice();

    AddressProjection getBranchAddress();
}