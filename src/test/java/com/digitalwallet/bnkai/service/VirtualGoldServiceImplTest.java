package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.entity.*;
import com.digitalwallet.bnkai.exception.*;
import com.digitalwallet.bnkai.mapper.HoldingMapper;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.repository.VirtualGoldHoldingRepository;
import com.digitalwallet.bnkai.service.impl.VirtualGoldServiceImpl;
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
class VirtualGoldServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorBranchRepository vendorBranchRepository;

    @Mock
    private VirtualGoldHoldingRepository holdingRepository;

    @Mock
    private BranchAllocationService branchAllocationService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TransactionHistoryService transactionHistoryService;

    @Mock
    private HoldingMapper holdingMapper;

    @InjectMocks
    private VirtualGoldServiceImpl virtualGoldService;

    private User user;

    private Vendor vendor;

    private VendorBranch branch;

    private Address address;

    private BuyVirtualGoldRequest buyRequest;

    private SellVirtualGoldRequest sellRequest;

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

        buyRequest = new BuyVirtualGoldRequest();

        buyRequest.setUserId(1);

        buyRequest.setVendorId(1);

        buyRequest.setQuantity(
                new BigDecimal("1")
        );

        sellRequest = new SellVirtualGoldRequest();

        sellRequest.setUserId(1);

        sellRequest.setHoldingId(1);

        sellRequest.setQuantity(
                new BigDecimal("1")
        );

        lenient().when(vendorBranchRepository.findByBranchIdForUpdate(anyInt()))
                 .thenReturn(Optional.of(branch));
    }

    // =========================================
    // BUY FLOW TESTS
    // =========================================

    @Test
    void buyVirtualGold_ShouldBuySuccessfully() {

        when(holdingMapper.toEntity(any(User.class), any(VendorBranch.class), any(BigDecimal.class), any()))
                .thenAnswer(invocation -> new VirtualGoldHolding(
                        null,
                        invocation.getArgument(0),
                        invocation.getArgument(1),
                        invocation.getArgument(2),
                        invocation.getArgument(3)
                ));

        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    VirtualGoldHolding h = invocation.getArgument(0);
                    HoldingDTO dto = new HoldingDTO();
                    dto.setQuantity(h.getQuantity());
                    return dto;
                });

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
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        when(
                holdingRepository.findByUserUserIdAndBranchBranchIdForUpdate(
                        1,
                        1
                )
        ).thenReturn(
                Optional.empty()
        );

        when(
                holdingRepository.save(
                        any(VirtualGoldHolding.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        HoldingDTO holding =
                virtualGoldService.buyVirtualGold(
                        buyRequest
                );

        assertNotNull(holding);

        assertEquals(
                0,
                new BigDecimal("1")
                        .compareTo(
                                holding.getQuantity()
                        )
        );

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
    void buyVirtualGold_ShouldIncreaseExistingHoldingQuantity() {

        VirtualGoldHolding existingHolding =
                new VirtualGoldHolding();

        existingHolding.setUser(user);

        existingHolding.setBranch(branch);

        existingHolding.setQuantity(
                new BigDecimal("2")
        );

        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    VirtualGoldHolding h = invocation.getArgument(0);
                    HoldingDTO dto = new HoldingDTO();
                    dto.setQuantity(h.getQuantity());
                    return dto;
                });

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
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        when(
                holdingRepository.findByUserUserIdAndBranchBranchIdForUpdate(
                        1,
                        1
                )
        ).thenReturn(
                Optional.of(existingHolding)
        );

        when(
                holdingRepository.save(
                        any(VirtualGoldHolding.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        HoldingDTO updatedHolding =
                virtualGoldService.buyVirtualGold(
                        buyRequest
                );

        assertEquals(
                0,
                new BigDecimal("3")
                        .compareTo(
                                updatedHolding.getQuantity()
                        )
        );
    }

    @Test
    void buyVirtualGold_ShouldThrowInvalidQuantityException() {

        buyRequest.setQuantity(
                BigDecimal.ZERO
        );

        assertThrows(
                InvalidQuantityException.class,
                () ->
                        virtualGoldService.buyVirtualGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyVirtualGold_ShouldThrowUserNotFoundException() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.empty()
        );

        assertThrows(
                UserNotFoundException.class,
                () ->
                        virtualGoldService.buyVirtualGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyVirtualGold_ShouldThrowAddressNotFoundException() {

        user.setAddress(null);

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        assertThrows(
                AddressNotFoundException.class,
                () ->
                        virtualGoldService.buyVirtualGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyVirtualGold_ShouldThrowVendorNotFoundException() {

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
                        virtualGoldService.buyVirtualGold(
                                buyRequest
                        )
        );
    }

    @Test
    void buyVirtualGold_ShouldThrowInsufficientWalletBalanceException() {

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
                branchAllocationService.allocateBranch(
                        anyInt(),
                        anyInt(),
                        any(BigDecimal.class)
                )
        ).thenReturn(branch);

        assertThrows(
                InsufficientWalletBalanceException.class,
                () ->
                        virtualGoldService.buyVirtualGold(
                                buyRequest
                        )
        );
    }

    // =========================================
    // SELL FLOW TESTS
    // =========================================

    @Test
    void sellVirtualGold_ShouldSellSuccessfully() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setHoldingId(1);

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("5")
        );

        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    VirtualGoldHolding h = invocation.getArgument(0);
                    HoldingDTO dto = new HoldingDTO();
                    dto.setHoldingId(h.getHoldingId());
                    dto.setQuantity(h.getQuantity());
                    return dto;
                });

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        when(
                holdingRepository.save(
                        any(VirtualGoldHolding.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        HoldingDTO updatedHolding =
                virtualGoldService.sellVirtualGold(
                        sellRequest
                );

        assertEquals(
                0,
                new BigDecimal("4")
                        .compareTo(
                                updatedHolding.getQuantity()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("15000")
                        .compareTo(
                                user.getBalance()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("101")
                        .compareTo(
                                branch.getQuantity()
                        )
        );

        verify(paymentService)
                .createWalletCreditEntry(
                        eq(user),
                        eq(new BigDecimal("5000.00")),
                        eq(PaymentConstants.BANK_TRANSFER),
                        eq(PaymentConstants.SUCCESS)
                );

        verify(transactionHistoryService)
                .createSellTransaction(
                        eq(user),
                        eq(branch),
                        eq(new BigDecimal("1")),
                        eq(new BigDecimal("5000.00")),
                        eq(TransactionConstants.SUCCESS)
                );
    }

    @Test
    void sellVirtualGold_ShouldThrowHoldingNotFoundException() {

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
                        virtualGoldService.sellVirtualGold(
                                sellRequest
                        )
        );
    }

    @Test
    void sellVirtualGold_ShouldThrowUnauthorizedHoldingAccessException() {

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
                        virtualGoldService.sellVirtualGold(
                                sellRequest
                        )
        );
    }

    @Test
    void sellVirtualGold_ShouldThrowInsufficientHoldingQuantityException() {

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
                        virtualGoldService.sellVirtualGold(
                                sellRequest
                        )
        );
    }

    @Test
    void sellVirtualGold_ShouldDeleteHolding_WhenQuantityBecomesZero() {

        when(
                userRepository.findByUserIdForUpdate(1)
        ).thenReturn(
                Optional.of(user)
        );

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setHoldingId(1);

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(
                new BigDecimal("1")
        );

        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    VirtualGoldHolding h = invocation.getArgument(0);
                    HoldingDTO dto = new HoldingDTO();
                    dto.setHoldingId(h.getHoldingId());
                    dto.setQuantity(h.getQuantity());
                    return dto;
                });

        when(
                holdingRepository.findByHoldingIdForUpdate(1)
        ).thenReturn(
                Optional.of(holding)
        );

        when(
                vendorBranchRepository.save(
                        any(VendorBranch.class)
                )
        ).thenReturn(branch);

        virtualGoldService.sellVirtualGold(
                sellRequest
        );

        verify(holdingRepository)
                .delete(holding);
    }
}
