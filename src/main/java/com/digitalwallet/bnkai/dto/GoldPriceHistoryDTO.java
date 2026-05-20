package com.digitalwallet.bnkai.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoldPriceHistoryDTO {
    private String date;
    private BigDecimal price;
}
