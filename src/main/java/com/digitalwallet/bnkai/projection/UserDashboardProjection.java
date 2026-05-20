package com.digitalwallet.bnkai.projection;

import java.math.BigDecimal;

public interface UserDashboardProjection {

    String getName();

    String getEmail();

    BigDecimal getBalance();

    AddressProjection getAddress();
}