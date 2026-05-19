package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;

public interface PhysicalGoldService {

    PhysicalGoldTransaction buyPhysicalGold(
            BuyPhysicalGoldRequest request
    );

    PhysicalGoldTransaction
    convertToPhysicalGold(
            ConvertToPhysicalGoldRequest request
    );
}