package com.digitalwallet.bnkai.mapper;

import com.digitalwallet.bnkai.dto.AddBranchRequest;
import com.digitalwallet.bnkai.dto.EditVendorProfileRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.dto.UserDTO;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MapperIntegrationTest {

    private final AddressMapper addressMapper = Mappers.getMapper(AddressMapper.class);
    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final VendorMapper vendorMapper = Mappers.getMapper(VendorMapper.class);
    private final HoldingMapper holdingMapper = Mappers.getMapper(HoldingMapper.class);
    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);
    private final PhysicalGoldMapper physicalGoldMapper = Mappers.getMapper(PhysicalGoldMapper.class);

    MapperIntegrationTest() {
        ReflectionTestUtils.setField(userMapper, "addressMapper", addressMapper);
        ReflectionTestUtils.setField(holdingMapper, "addressMapper", addressMapper);
        ReflectionTestUtils.setField(physicalGoldMapper, "addressMapper", addressMapper);
    }

    @Test
    void userToDto_mapsNestedAddressAndIgnoresPassword() {
        User user = new User();
        user.setUserId(1);
        user.setName("Pradeep Kumar");
        user.setEmail("pradeep@example.in");
        user.setPassword("encoded-secret");
        user.setBalance(new BigDecimal("1500.00"));
        user.setAddress(address(3, "MG Road", "Bengaluru", "Karnataka", "560001"));

        UserDTO dto = userMapper.toDto(user);

        assertThat(dto.getUserId()).isEqualTo(1);
        assertThat(dto.getBalance()).isEqualByComparingTo("1500.00");
        assertThat(dto.getAddress().getPostalCode()).isEqualTo("560001");
    }

    @Test
    void holdingToDto_handlesNestedVendorAddressComputedValueAndNulls() {
        Vendor vendor = new Vendor();
        vendor.setVendorName("Sona Jewelers");
        VendorBranch branch = new VendorBranch();
        branch.setVendor(vendor);
        branch.setAddress(address(7, "Link Road", "Mumbai", "Maharashtra", "400001"));
        VirtualGoldHolding holding = new VirtualGoldHolding();
        holding.setHoldingId(10);
        holding.setBranch(branch);
        holding.setQuantity(new BigDecimal("2.50"));

        HoldingDTO dto = holdingMapper.toDto(holding, new BigDecimal("7150.00"));

        assertThat(dto.getVendorName()).isEqualTo("Sona Jewelers");
        assertThat(dto.getBranchAddress().getCity()).isEqualTo("Mumbai");
        assertThat(dto.getCurrentValue()).isEqualByComparingTo("17875.00");
        assertThat(holdingMapper.toDto(null, BigDecimal.ONE)).isNull();
    }

    @Test
    void transactionToDto_mapsVendorUserAndBranchSummariesSafely() {
        User user = new User();
        user.setName("Asha");
        user.setAddress(address(1, "Race Course", "Delhi", "Delhi", "110001"));
        Vendor vendor = new Vendor();
        vendor.setVendorName("Golden Heritage");
        VendorBranch branch = new VendorBranch();
        branch.setBranchId(4);
        branch.setVendor(vendor);
        branch.setAddress(address(2, "Fort", "Mumbai", "Maharashtra", "400001"));
        TransactionHistory transaction = transactionMapper.toEntity(
                user,
                branch,
                new BigDecimal("1.25"),
                new BigDecimal("8937.50"),
                "Buy",
                "Success",
                LocalDateTime.of(2026, 5, 18, 12, 0)
        );

        TransactionDTO dto = transactionMapper.toDto(transaction);

        assertThat(dto.getVendorName()).isEqualTo("Golden Heritage");
        assertThat(dto.getBranchName()).isEqualTo("Branch #4");
        assertThat(dto.getBranchAddress()).isEqualTo("Mumbai, Maharashtra");
        assertThat(dto.getUserName()).isEqualTo("Asha");
        assertThat(dto.getUserAddress()).isEqualTo("Delhi, Delhi");
    }

    @Test
    void requestAndPatchMappers_ignoreIdsAndApplyNullSafeUpdates() {
        AddBranchRequest request = new AddBranchRequest();
        request.setStreet("Park Street");
        request.setCity("Kolkata");
        request.setState("West Bengal");
        request.setPostalCode("700016");
        request.setCountry("India");

        Address address = addressMapper.toEntity(request);
        assertThat(address.getAddressId()).isNull();
        assertThat(address.getPostalCode()).isEqualTo("700016");

        Vendor vendor = new Vendor();
        vendor.setContactPersonName("Old Name");
        vendor.setContactEmail("old@example.in");
        EditVendorProfileRequest patch = new EditVendorProfileRequest();
        patch.setContactEmail("new@example.in");
        vendorMapper.updateProfile(patch, vendor);

        assertThat(vendor.getContactPersonName()).isEqualTo("Old Name");
        assertThat(vendor.getContactEmail()).isEqualTo("new@example.in");
    }

    @Test
    void physicalGoldMapper_mapsCollectionAndDeliveryAddressWithoutCycles() {
        Vendor vendor = new Vendor();
        vendor.setVendorName("Sona Jewelers");
        VendorBranch branch = new VendorBranch();
        branch.setVendor(vendor);
        PhysicalGoldTransaction transaction = physicalGoldMapper.toEntity(
                new User(),
                branch,
                address(5, "Civil Lines", "Jaipur", "Rajasthan", "302006"),
                new BigDecimal("1.00"),
                LocalDateTime.now()
        );

        List<PhysicalGoldDTO> dtos = physicalGoldMapper.toDtoList(List.of(transaction));

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).getVendorName()).isEqualTo("Sona Jewelers");
        assertThat(dtos.get(0).getDeliveryAddress().getCity()).isEqualTo("Jaipur");
    }

    private Address address(Integer id, String street, String city, String state, String postalCode) {
        Address address = new Address();
        address.setAddressId(id);
        address.setStreet(street);
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountry("India");
        return address;
    }
}
