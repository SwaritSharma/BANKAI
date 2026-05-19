package com.digitalwallet.bnkai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardDTO {
    @JsonProperty("name")
    private String name;

    @JsonProperty("email")
    private String email;

    @JsonProperty("pnl_amount")
    private BigDecimal pnlAmount;

    @JsonProperty("balance")
    private BigDecimal balance;

    @JsonProperty("total_holdings_grams")
    private BigDecimal totalHoldingsGrams;

    @JsonProperty("total_holdings_value")
    private BigDecimal totalHoldingsValue;

    @JsonProperty("current_gold_price")
    private BigDecimal currentGoldPrice;

    @JsonProperty("pnl_percent")
    private BigDecimal pnlPercent;
}
