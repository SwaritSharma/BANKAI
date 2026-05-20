package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.GoldPriceHistoryDTO;
import com.digitalwallet.bnkai.service.GoldPriceService;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.GOLD_PRICE_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.GOLD_PRICE_HISTORY_CACHE;

@Service
public class GoldPriceServiceImpl implements GoldPriceService {

    private static final BigDecimal CURRENT_PRICE = new BigDecimal("7150.00");
    private static final BigDecimal CHANGE_24H = new BigDecimal("45.50");
    private static final BigDecimal CHANGE_PCT = new BigDecimal("0.64");

    @Override
    @Cacheable(cacheNames = GOLD_PRICE_CACHE, key = "'current'")
    public GoldPriceDTO getCurrentPrice() {
        return buildCurrentPrice();
    }

    @Override
    @Cacheable(cacheNames = GOLD_PRICE_HISTORY_CACHE, key = "#days")
    public List<GoldPriceHistoryDTO> getPriceHistory(int days) {
        List<GoldPriceHistoryDTO> history = new ArrayList<>();
        LocalDate date = LocalDate.now().minusDays(days);
        BigDecimal price = new BigDecimal("7000.00");
        Random random = new Random(42);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < days; i++) {
            GoldPriceHistoryDTO dto = new GoldPriceHistoryDTO();
            dto.setDate(date.plusDays(i).format(formatter));

            BigDecimal change = BigDecimal.valueOf(random.nextDouble() * 100 - 40);
            price = price.add(change).setScale(2, RoundingMode.HALF_UP);

            dto.setPrice(price);
            history.add(dto);
        }

        if (!history.isEmpty()) {
            history.get(history.size() - 1).setPrice(CURRENT_PRICE);
        }

        return history;
    }

    @Override
    @CachePut(cacheNames = GOLD_PRICE_CACHE, key = "'current'")
    public GoldPriceDTO refreshCurrentPrice() {
        return buildCurrentPrice();
    }

    private GoldPriceDTO buildCurrentPrice() {
        GoldPriceDTO dto = new GoldPriceDTO();
        dto.setPrice(CURRENT_PRICE);
        dto.setChange24h(CHANGE_24H);
        dto.setChangePct(CHANGE_PCT);
        return dto;
    }
}
