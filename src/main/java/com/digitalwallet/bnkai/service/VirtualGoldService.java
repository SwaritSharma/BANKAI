package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;

public interface VirtualGoldService {

    HoldingDTO buyVirtualGold(
            BuyVirtualGoldRequest request
    );

    HoldingDTO sellVirtualGold(
            SellVirtualGoldRequest request
    );
}