package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;

public interface BranchSummaryProjection {

    BigDecimal getQuantity();

    AddressProjection getAddress();

    VendorProjection getVendor();

    interface VendorProjection {

        String getVendorName();

    }
}