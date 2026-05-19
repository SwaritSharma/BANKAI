package com.digitalwallet.bnkai.mapper;


import com.digitalwallet.bnkai.dto.*;
import com.digitalwallet.bnkai.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AddressMapper {

    HoldingDTO.AddressDTO toDto(Address address);

    @Mapping(target = "addressId", ignore = true)
    Address toEntity(AddBranchRequest request);

    @Mapping(target = "addressId", ignore = true)
    Address toEntity(RegisterRequest request);

    @Mapping(target = "addressId", ignore = true)
    Address toEntity(VendorRegisterRequest request);

    @Mapping(target = "addressId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromRegister(RegisterRequest request, @MappingTarget Address address);

    @Mapping(target = "addressId", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateAddressFromProfile(EditProfileRequest request, @MappingTarget Address address);
}
