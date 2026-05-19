package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;

public interface PhysicalGoldService {

    PhysicalGoldDTO buyPhysicalGold(
            BuyPhysicalGoldRequest request
    );

    PhysicalGoldDTO convertToPhysicalGold(
            ConvertToPhysicalGoldRequest request
    );
}