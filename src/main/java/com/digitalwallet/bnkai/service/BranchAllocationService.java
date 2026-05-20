package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.entity.VendorBranch;

import java.math.BigDecimal;

public interface BranchAllocationService {

    VendorBranch allocateBranch(
            Integer vendorId,
            Integer deliveryAddressId,
            BigDecimal requiredQuantity
    );
}