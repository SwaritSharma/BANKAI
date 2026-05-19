package com.digitalwallet.bnkai.mapper;

import com.digitalwallet.bnkai.dto.DashboardDTO;
import com.digitalwallet.bnkai.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface DashboardMapper {

    @Mapping(target = "name", source = "user.name")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "balance", source = "balance")
    @Mapping(target = "totalHoldingsGrams", source = "totalGrams")
    @Mapping(target = "totalHoldingsValue", source = "totalValue")
    @Mapping(target = "currentGoldPrice", source = "currentGoldPrice")
    @Mapping(target = "pnlAmount", source = "pnlAmount")
    @Mapping(target = "pnlPercent", source = "pnlPercent")
    DashboardDTO toDashboard(
            User user,
            BigDecimal balance,
            BigDecimal totalGrams,
            BigDecimal totalValue,
            BigDecimal currentGoldPrice,
            BigDecimal pnlAmount,
            BigDecimal pnlPercent
    );
}
