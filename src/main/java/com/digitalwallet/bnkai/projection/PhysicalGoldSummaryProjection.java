package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PhysicalGoldSummaryProjection {

    String getVendorName();

    BigDecimal getQuantity();

    AddressProjection getBranchAddress();

    AddressProjection getDeliveryAddress();

    LocalDateTime getCreatedAt();
}