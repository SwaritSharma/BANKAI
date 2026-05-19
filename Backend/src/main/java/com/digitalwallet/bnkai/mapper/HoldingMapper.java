package com.digitalwallet.bnkai.mapper;

import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface HoldingMapper {

    @Mapping(target = "vendorName", source = "holding", qualifiedByName = "vendorName")
    @Mapping(target = "branchAddress", source = "branch.address")
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "currentGoldPrice", ignore = true)
    @Mapping(target = "value", ignore = true)
    HoldingDTO toDto(VirtualGoldHolding holding, @Context BigDecimal currentGoldPrice);

    List<HoldingDTO> toDtoList(List<VirtualGoldHolding> holdings, @Context BigDecimal currentGoldPrice);

    @Mapping(target = "holdingId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "branch", source = "branch")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "createdAt", source = "createdAt")
    VirtualGoldHolding toEntity(User user, VendorBranch branch, BigDecimal quantity, LocalDateTime createdAt);

    @AfterMapping
    default void addComputedValues(
            VirtualGoldHolding holding,
            @Context BigDecimal currentGoldPrice,
            @MappingTarget HoldingDTO dto
    ) {
        BigDecimal quantity = holding != null && holding.getQuantity() != null ? holding.getQuantity() : BigDecimal.ZERO;
        BigDecimal price = currentGoldPrice != null ? currentGoldPrice : BigDecimal.ZERO;
        BigDecimal value = quantity.multiply(price).setScale(2, RoundingMode.HALF_UP);
        dto.setQuantity(quantity);
        dto.setCurrentGoldPrice(price);
        dto.setCurrentValue(value);
        dto.setValue(value);
    }

    @Named("vendorName")
    default String vendorName(VirtualGoldHolding holding) {
        if (holding == null || holding.getBranch() == null || holding.getBranch().getVendor() == null) {
            return "Unknown Vendor";
        }
        return holding.getBranch().getVendor().getVendorName();
    }
}
