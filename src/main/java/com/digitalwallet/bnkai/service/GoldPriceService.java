package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.GoldPriceHistoryDTO;

import java.util.List;

public interface GoldPriceService {

    GoldPriceDTO getCurrentPrice();

    List<GoldPriceHistoryDTO> getPriceHistory(int days);

    GoldPriceDTO refreshCurrentPrice();
}
