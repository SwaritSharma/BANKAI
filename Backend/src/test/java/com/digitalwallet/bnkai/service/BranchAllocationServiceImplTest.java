package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.exception.AddressNotFoundException;
import com.digitalwallet.bnkai.exception.BranchAllocationException;
import com.digitalwallet.bnkai.repository.AddressRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.service.impl.BranchAllocationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchAllocationServiceImplTest {

    @Mock
    private VendorBranchRepository
            vendorBranchRepository;

    @Mock
    private AddressRepository
            addressRepository;

    @InjectMocks
    private BranchAllocationServiceImpl
            branchAllocationService;

    private Address deliveryAddress;

    private Vendor vendor;

    private VendorBranch postalCodeBranch;

    private VendorBranch cityBranch;

    private VendorBranch stateBranch;

    private VendorBranch maxQuantityBranch;

    @BeforeEach
    void setUp() {

        deliveryAddress =
                new Address();

        deliveryAddress.setAddressId(1);

        deliveryAddress.setPostalCode("110001");

        deliveryAddress.setCity("Delhi");

        deliveryAddress.setState("Delhi");

        vendor =
                new Vendor();

        vendor.setVendorId(1);

        postalCodeBranch =
                new VendorBranch();

        postalCodeBranch.setBranchId(1);

        postalCodeBranch.setVendor(vendor);

        postalCodeBranch.setQuantity(
                new BigDecimal("100")
        );

        Address postalAddress =
                new Address();

        postalAddress.setPostalCode("110001");

        postalAddress.setCity("Delhi");

        postalAddress.setState("Delhi");

        postalCodeBranch.setAddress(
                postalAddress
        );

        cityBranch =
                new VendorBranch();

        cityBranch.setBranchId(2);

        cityBranch.setVendor(vendor);

        cityBranch.setQuantity(
                new BigDecimal("80")
        );

        Address cityAddress =
                new Address();

        cityAddress.setPostalCode("220001");

        cityAddress.setCity("Delhi");

        cityAddress.setState("Delhi");

        cityBranch.setAddress(
                cityAddress
        );

        stateBranch =
                new VendorBranch();

        stateBranch.setBranchId(3);

        stateBranch.setVendor(vendor);

        stateBranch.setQuantity(
                new BigDecimal("60")
        );

        Address stateAddress =
                new Address();

        stateAddress.setPostalCode("330001");

        stateAddress.setCity("Noida");

        stateAddress.setState("Delhi");

        stateBranch.setAddress(
                stateAddress
        );

        maxQuantityBranch =
                new VendorBranch();

        maxQuantityBranch.setBranchId(4);

        maxQuantityBranch.setVendor(vendor);

        maxQuantityBranch.setQuantity(
                new BigDecimal("500")
        );

        Address maxAddress =
                new Address();

        maxAddress.setPostalCode("440001");

        maxAddress.setCity("Mumbai");

        maxAddress.setState("Maharashtra");

        maxQuantityBranch.setAddress(
                maxAddress
        );
    }

    @Test
    void allocateBranch_ShouldAllocatePostalCodeBranch() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(deliveryAddress)
        );

        when(
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                1,
                                "110001",
                                new BigDecimal("10")
                        )
        ).thenReturn(
                Optional.of(postalCodeBranch)
        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                1,
                                1,
                                new BigDecimal("10")
                        );

        assertNotNull(
                allocatedBranch
        );

        assertEquals(
                1,
                allocatedBranch.getBranchId()
        );
    }

    @Test
    void allocateBranch_ShouldAllocateSameCityBranch() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(deliveryAddress)
        );

        when(
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                anyInt(),
                                anyString(),
                                any(BigDecimal.class)
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                vendorBranchRepository
                        .findByVendorVendorIdAndQuantityGreaterThanEqual(
                                1,
                                new BigDecimal("10")
                        )
        ).thenReturn(
                List.of(cityBranch)
        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                1,
                                1,
                                new BigDecimal("10")
                        );

        assertEquals(
                2,
                allocatedBranch.getBranchId()
        );
    }

    @Test
    void allocateBranch_ShouldAllocateSameStateBranch() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(deliveryAddress)
        );

        when(
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                anyInt(),
                                anyString(),
                                any(BigDecimal.class)
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                vendorBranchRepository
                        .findByVendorVendorIdAndQuantityGreaterThanEqual(
                                1,
                                new BigDecimal("10")
                        )
        ).thenReturn(
                List.of(stateBranch)
        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                1,
                                1,
                                new BigDecimal("10")
                        );

        assertEquals(
                3,
                allocatedBranch.getBranchId()
        );
    }

    @Test
    void allocateBranch_ShouldAllocateHighestQuantityBranch() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(deliveryAddress)
        );

        when(
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                anyInt(),
                                anyString(),
                                any(BigDecimal.class)
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                vendorBranchRepository
                        .findByVendorVendorIdAndQuantityGreaterThanEqual(
                                1,
                                new BigDecimal("10")
                        )
        ).thenReturn(
                List.of(maxQuantityBranch)
        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                1,
                                1,
                                new BigDecimal("10")
                        );

        assertEquals(
                4,
                allocatedBranch.getBranchId()
        );
    }

    @Test
    void allocateBranch_ShouldThrowAddressNotFoundException() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AddressNotFoundException.class,
                () ->
                        branchAllocationService
                                .allocateBranch(
                                        1,
                                        1,
                                        new BigDecimal("10")
                                )
        );
    }

    @Test
    void allocateBranch_ShouldThrowBranchAllocationException_WhenNoBranchFound() {

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(deliveryAddress)
        );

        when(
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                anyInt(),
                                anyString(),
                                any(BigDecimal.class)
                        )
        ).thenReturn(
                Optional.empty()
        );

        when(
                vendorBranchRepository
                        .findByVendorVendorIdAndQuantityGreaterThanEqual(
                                1,
                                new BigDecimal("10")
                        )
        ).thenReturn(
                List.of()
        );

        assertThrows(
                BranchAllocationException.class,
                () ->
                        branchAllocationService
                                .allocateBranch(
                                        1,
                                        1,
                                        new BigDecimal("10")
                                )
        );
    }
}