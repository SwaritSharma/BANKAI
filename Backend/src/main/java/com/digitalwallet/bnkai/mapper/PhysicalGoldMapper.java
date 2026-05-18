package com.digitalwallet.bnkai.mapper;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VendorBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = AddressMapper.class)
public interface PhysicalGoldMapper {

    @Mapping(target = "vendorName", source = "transaction", qualifiedByName = "vendorName")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    PhysicalGoldDTO toDto(PhysicalGoldTransaction transaction);

    List<PhysicalGoldDTO> toDtoList(List<PhysicalGoldTransaction> transactions);

    @Mapping(target = "physicalTransactionId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "branch", source = "branch")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "createdAt", source = "createdAt")
    PhysicalGoldTransaction toEntity(
            User user,
            VendorBranch branch,
            Address deliveryAddress,
            BigDecimal quantity,
            LocalDateTime createdAt
    );

    @Named("vendorName")
    default String vendorName(PhysicalGoldTransaction transaction) {
        if (transaction == null || transaction.getBranch() == null || transaction.getBranch().getVendor() == null) {
            return "Unknown Vendor";
        }
        return transaction.getBranch().getVendor().getVendorName();
    }
}

