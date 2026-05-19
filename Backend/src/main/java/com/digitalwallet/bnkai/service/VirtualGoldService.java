package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;

public interface VirtualGoldService {

    VirtualGoldHolding buyVirtualGold(
            BuyVirtualGoldRequest request
    );

    VirtualGoldHolding sellVirtualGold(
            SellVirtualGoldRequest request
    );
}