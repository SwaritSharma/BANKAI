package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoldPriceDTO {
    private BigDecimal price;

    @JsonProperty("change_24h")
    private BigDecimal change24h;

    @JsonProperty("change_pct")
    private BigDecimal changePct;
}
