package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.entity.*;
import com.digitalwallet.bnkai.exception.*;
import com.digitalwallet.bnkai.mapper.PhysicalGoldMapper;
import com.digitalwallet.bnkai.repository.*;
import com.digitalwallet.bnkai.service.impl.PhysicalGoldServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhysicalGoldServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorBranchRepository vendorBranchRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private VirtualGoldHoldingRepository holdingRepository;

    @Mock
    private PhysicalGoldTransactionRepository physicalGoldTransactionRepository;

    @Mock
    private BranchAllocationService branchAllocationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private PhysicalGoldMapper physicalGoldMapper;

    @InjectMocks
    private PhysicalGoldServiceImpl physicalGoldService;

    private User user;

    private Vendor vendor;

    private VendorBranch branch;

    private Address address;

    private BuyPhysicalGoldRequest buyRequest;

    private ConvertToPhysicalGoldRequest convertRequest;

    @BeforeEach
    void setUp() {

        address = new Address();

        address.setAddressId(1);

        address.setCity("Delhi");

        user = new User();

        user.setUserId(1);

        user.setBalance(
                new BigDecimal("10000")
        );

        user.setAddress(address);

        vendor = new Vendor();

        vendor.setVendorId(1);

        vendor.setCurrentGoldPrice(
                new BigDecimal("5000")
        );

        branch = new VendorBranch();

        branch.setBranchId(1);

        branch.setVendor(vendor);

        branch.setQuantity(
                new BigDecimal("100")
        );

        buyRequest =
                new BuyPhysicalGoldRequest();

        buyRequest.setUserId(1);

        buyRequest.setVendorId(1);

        buyRequest.setDeliveryAddressId(1);

        buyRequest.setQuantity(
                new BigDecimal("1")
        );

        convertRequest =
                new ConvertToPhysicalGoldRequest();

        convertRequest.setUserId(1);

        convertRequest.setHoldingId(1);

        convertRequest.setDeliveryAddressId(1);

        convertRequest.setQuantity(
                new BigDecimal("1")
        );

        lenient().when(vendorBranchRepository.findByBranchIdForUpdate(anyInt()))
                 .thenReturn(Optional.of(branch));
    }

    // =========================================
    // BUY PHYSICAL GOLD TESTS
    // =========================================

    @Test
    void buyPhysicalGold_ShouldBuySuccessfully() {

        stubPhysicalGoldMapper();

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                vendorRepository.findById(1)
        ).thenReturn(
                Optional.of(vendor)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(address)
        );

        when(
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        when(
                physicalGoldTransactionRepository.save(
                        any(PhysicalGoldTransaction.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        PhysicalGoldDTO transaction =
                physicalGoldService.buyPhysicalGold(
                        buyRequest
                );

        assertNotNull(transaction);


        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(
                                user.getBalance()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("99")
                        .compareTo(
                                branch.getQuantity()
                        )
        );

        verify(paymentService)
                .createWalletDebitEntry(
                        eq(user),
                        eq(new BigDecimal("5000.00")),
                        eq(PaymentConstants.BANK_TRANSFER),
                        eq(PaymentConstants.SUCCESS)
                );

        verify(transactionHistoryService)
                .createBuyTransaction(
                        eq(user),
                        eq(branch),
                        eq(new BigDecimal("1")),
                        eq(new BigDecimal("5000.00")),
                        eq(TransactionConstants.SUCCESS)
                );
    }

    @Test
    void buyPhysicalGold_ShouldThrowInvalidQuantityException() {

        buyRequest.setQuantity(
                BigDecimal.ZERO
        );

        assertThrows(
                InvalidQuantityException.class,
                () ->
                        physicalGoldService.buyPhysicalGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyPhysicalGold_ShouldThrowUserNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UserNotFoundException.class,
                () ->
                        physicalGoldService.buyPhysicalGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyPhysicalGold_ShouldThrowVendorNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                vendorRepository.findById(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                VendorNotFoundException.class,
                () ->
                        physicalGoldService.buyPhysicalGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyPhysicalGold_ShouldThrowAddressNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                vendorRepository.findById(1)
        ).thenReturn(
                Optional.of(vendor)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AddressNotFoundException.class,
                () ->
                        physicalGoldService.buyPhysicalGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyPhysicalGold_ShouldThrowInsufficientWalletBalanceException() {

        user.setBalance(
                new BigDecimal("100")
        );

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                vendorRepository.findById(1)
        ).thenReturn(
                Optional.of(vendor)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(address)
        );

        when(
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        assertThrows(
                InsufficientWalletBalanceException.class,
                () ->
                        physicalGoldService.buyPhysicalGold(
                                buyRequest
                        )
        );
    }

    // =========================================
    // CONVERT TO PHYSICAL TESTS
    // =========================================

    @Test
    void convertToPhysicalGold_ShouldConvertSuccessfully() {

        stubPhysicalGoldMapper();

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(address)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setHoldingId(1);

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("5")
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        when(
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        when(
                holdingRepository.save(
                        any(VirtualGoldHolding.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                physicalGoldTransactionRepository.save(
                        any(PhysicalGoldTransaction.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        PhysicalGoldDTO transaction =
                physicalGoldService.convertToPhysicalGold(
                        convertRequest
                );

        assertNotNull(transaction);

        assertEquals(
                0,
                new BigDecimal("4")
                        .compareTo(
                                holding.getQuantity()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("100")
                        .compareTo(
                                branch.getQuantity()
                        )
        );

        verify(paymentService,
                never()
        ).createWalletDebitEntry(
                any(),
                any(),
                anyString(),
                anyString()
        );

        verify(transactionHistoryService)
                .createConvertToPhysicalTransaction(
                        eq(user),
                        eq(branch),
                        eq(new BigDecimal("1")),
                        eq(new BigDecimal("5000.00")),
                        eq(TransactionConstants.SUCCESS)
                );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowInvalidQuantityException() {

        convertRequest.setQuantity(
                BigDecimal.ZERO
        );

        assertThrows(
                InvalidQuantityException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowUserNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UserNotFoundException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowAddressNotFoundException_WhenUserAddressMissing() {

        user.setAddress(null);

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        assertThrows(
                AddressNotFoundException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowHoldingNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                HoldingNotFoundException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowUnauthorizedHoldingAccessException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        User anotherUser =
                new User();

        anotherUser.setUserId(999);

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setUser(anotherUser);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("5")
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        assertThrows(
                UnauthorizedHoldingAccessException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowAddressNotFoundException_WhenDeliveryAddressMissing() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("5")
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                AddressNotFoundException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldThrowInsufficientHoldingQuantityException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(address)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("0.5")
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        assertThrows(
                InsufficientHoldingQuantityException.class,
                () ->
                        physicalGoldService.convertToPhysicalGold(
                                convertRequest
                        )
        );
    }

    @Test
    void convertToPhysicalGold_ShouldDeleteHolding_WhenQuantityBecomesZero() {

        stubPhysicalGoldMapper();

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        when(
                addressRepository.findById(1)
        ).thenReturn(
                Optional.of(address)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setHoldingId(1);

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("1")
        );

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        when(
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        when(
                physicalGoldTransactionRepository.save(
                        any(PhysicalGoldTransaction.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        physicalGoldService.convertToPhysicalGold(
                convertRequest
        );

        verify(holdingRepository)
                .delete(holding);
    }

    private void stubPhysicalGoldMapper() {
        when(
                physicalGoldMapper.toEntity(
                        any(User.class),
                        any(VendorBranch.class),
                        any(Address.class),
                        any(BigDecimal.class),
                        any()
                )
        ).thenAnswer(invocation -> new PhysicalGoldTransaction(
                null,
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3),
                invocation.getArgument(4)
        ));

        when(
                physicalGoldMapper.toDto(any(PhysicalGoldTransaction.class))
        ).thenAnswer(invocation -> {
            PhysicalGoldTransaction tx = invocation.getArgument(0);
            PhysicalGoldDTO dto = new PhysicalGoldDTO();
            dto.setPhysicalTransactionId(tx.getPhysicalTransactionId());
            dto.setQuantity(tx.getQuantity());
            dto.setCreatedAt(tx.getCreatedAt());
            return dto;
        });
    }
}
