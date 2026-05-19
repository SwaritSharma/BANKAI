package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.exception.*;
import com.digitalwallet.bnkai.mapper.HoldingMapper;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.repository.VirtualGoldHoldingRepository;
import com.digitalwallet.bnkai.service.BranchAllocationService;
import com.digitalwallet.bnkai.service.PaymentService;
import com.digitalwallet.bnkai.service.TransactionHistoryService;
import com.digitalwallet.bnkai.service.VirtualGoldService;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.*;

@Service
public class VirtualGoldServiceImpl
        implements VirtualGoldService {

    private final UserRepository
            userRepository;

    private final VendorRepository
            vendorRepository;

    private final VendorBranchRepository
            vendorBranchRepository;

    private final VirtualGoldHoldingRepository
            holdingRepository;

    private final BranchAllocationService
            branchAllocationService;

    private final PaymentService
            paymentService;

    private final
    TransactionHistoryService
            transactionHistoryService;

    private final HoldingMapper
            holdingMapper;

    public VirtualGoldServiceImpl(
            UserRepository userRepository,
            VendorRepository vendorRepository,
            VendorBranchRepository
                    vendorBranchRepository,
            VirtualGoldHoldingRepository
                    holdingRepository,
            BranchAllocationService
                    branchAllocationService,
            PaymentService paymentService,
            TransactionHistoryService
                    transactionHistoryService,
            HoldingMapper holdingMapper
    ) {

        this.userRepository =
                userRepository;

        this.vendorRepository =
                vendorRepository;

        this.vendorBranchRepository =
                vendorBranchRepository;

        this.holdingRepository =
                holdingRepository;

        this.branchAllocationService =
                branchAllocationService;

        this.paymentService =
                paymentService;

        this.transactionHistoryService =
                transactionHistoryService;

        this.holdingMapper =
                holdingMapper;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = {USER_DASHBOARD_CACHE, USER_HOLDINGS_CACHE, USER_TRANSACTIONS_CACHE, USER_PAYMENTS_CACHE}, key = "#request.userId"),
            @CacheEvict(cacheNames = {VENDOR_DASHBOARD_CACHE, VENDOR_BRANCHES_CACHE, VENDOR_TRANSACTIONS_CACHE, VENDORS_CACHE}, key = "#request.vendorId")
    })
    public VirtualGoldHolding
    buyVirtualGold(
            BuyVirtualGoldRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (
                request.getQuantity()
                        .compareTo(BigDecimal.ZERO)
                        <= 0
        ) {

            throw new InvalidQuantityException(
                    "Quantity must be greater than 0"
            );
        }

        User user =
                userRepository
                        .findById(
                                request.getUserId()
                        )
                        .orElseThrow(
                                () ->
                                        new UserNotFoundException(
                                                "User not found"
                                        )
                        );

        if (user.getAddress() == null) {

            throw new AddressNotFoundException(
                    "User address not found"
            );
        }

        Vendor vendor =
                vendorRepository
                        .findById(
                                request.getVendorId()
                        )
                        .orElseThrow(
                                () ->
                                        new VendorNotFoundException(
                                                "Vendor not found"
                                        )
                        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                vendor.getVendorId(),
                                user.getAddress()
                                        .getAddressId(),
                                request.getQuantity()
                        );

        BigDecimal totalAmount =
                allocatedBranch
                        .getVendor()
                        .getCurrentGoldPrice()
                        .multiply(
                                request.getQuantity()
                        );

        if (
                (user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
                        .compareTo(totalAmount)
                        < 0
        ) {

            throw new InsufficientWalletBalanceException(
                    "Insufficient wallet balance"
            );
        }

        user.setBalance(
                (user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
                        .subtract(totalAmount)
        );

        allocatedBranch.setQuantity(
                allocatedBranch.getQuantity()
                        .subtract(
                                request.getQuantity()
                        )
        );

        VirtualGoldHolding holding =
                holdingRepository
                        .findByUserUserIdAndBranchBranchId(
                                user.getUserId(),
                                allocatedBranch
                                        .getBranchId()
                        )
                        .orElse(null);

        if (holding == null) {

            holding = holdingMapper.toEntity(user, allocatedBranch, request.getQuantity(), LocalDateTime.now());
        }

        else {

            holding.setQuantity(
                    holding.getQuantity()
                            .add(
                                    request.getQuantity()
                            )
            );
        }

        paymentService
                .createWalletDebitEntry(
                        user,
                        totalAmount,
                        PaymentConstants
                                .BANK_TRANSFER,
                        PaymentConstants.SUCCESS
                );

        transactionHistoryService
                .createBuyTransaction(
                        user,
                        allocatedBranch,
                        request.getQuantity(),
                        totalAmount,
                        TransactionConstants.SUCCESS
                );

        userRepository.save(user);

        vendorBranchRepository
                .save(allocatedBranch);

        return holdingRepository
                .save(holding);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = {USER_DASHBOARD_CACHE, USER_HOLDINGS_CACHE, USER_TRANSACTIONS_CACHE, USER_PAYMENTS_CACHE}, key = "#request.userId"),
            @CacheEvict(cacheNames = {VENDOR_DASHBOARD_CACHE, VENDOR_BRANCHES_CACHE, VENDOR_TRANSACTIONS_CACHE, VENDORS_CACHE}, allEntries = true)
    })
    public VirtualGoldHolding
    sellVirtualGold(
            SellVirtualGoldRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (
                request.getQuantity()
                        .compareTo(BigDecimal.ZERO)
                        <= 0
        ) {

            throw new InvalidQuantityException(
                    "Quantity must be greater than 0"
            );
        }

        User user =
                userRepository
                        .findById(
                                request.getUserId()
                        )
                        .orElseThrow(
                                () ->
                                        new UserNotFoundException(
                                                "User not found"
                                        )
                        );

        VirtualGoldHolding holding =
                holdingRepository
                        .findById(
                                request.getHoldingId()
                        )
                        .orElseThrow(
                                () ->
                                        new HoldingNotFoundException(
                                                "Holding not found"
                                        )
                        );

        if (
                !holding.getUser()
                        .getUserId()
                        .equals(
                                request.getUserId()
                        )
        ) {

            throw new UnauthorizedHoldingAccessException(
                    "Holding does not belong to user"
            );
        }

        if (
                holding.getQuantity()
                        .compareTo(
                                request.getQuantity()
                        )
                        < 0
        ) {

            throw new InsufficientHoldingQuantityException(
                    "Insufficient holding quantity"
            );
        }

        VendorBranch branch =
                holding.getBranch();

        BigDecimal totalAmount =
                branch.getVendor()
                        .getCurrentGoldPrice()
                        .multiply(
                                request.getQuantity()
                        );

        user.setBalance(
                (user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO)
                        .add(totalAmount)
        );

        branch.setQuantity(
                branch.getQuantity()
                        .add(
                                request.getQuantity()
                        )
        );

        holding.setQuantity(
                holding.getQuantity()
                        .subtract(
                                request.getQuantity()
                        )
        );

        paymentService
                .createWalletCreditEntry(
                        user,
                        totalAmount,
                        PaymentConstants
                                .BANK_TRANSFER,
                        PaymentConstants.SUCCESS
                );

        transactionHistoryService
                .createSellTransaction(
                        user,
                        branch,
                        request.getQuantity(),
                        totalAmount,
                        TransactionConstants.SUCCESS
                );

        userRepository.save(user);

        vendorBranchRepository
                .save(branch);

        if (
                holding.getQuantity()
                        .compareTo(BigDecimal.ZERO)
                        == 0
        ) {

            holdingRepository.delete(
                    holding
            );

            return holding;
        }

        return holdingRepository
                .save(holding);
    }
}
