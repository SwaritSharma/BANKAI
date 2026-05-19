package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.service.PhysicalGoldService;
import com.digitalwallet.bnkai.service.BranchAllocationService;
import com.digitalwallet.bnkai.service.TransactionHistoryService;
import com.digitalwallet.bnkai.service.PaymentService;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.exception.*;
import com.digitalwallet.bnkai.mapper.PhysicalGoldMapper;
import com.digitalwallet.bnkai.repository.AddressRepository;
import com.digitalwallet.bnkai.repository.PhysicalGoldTransactionRepository;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.repository.VirtualGoldHoldingRepository;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_DASHBOARD_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_HOLDINGS_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_PAYMENTS_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_PHYSICAL_GOLD_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_TRANSACTIONS_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.VENDOR_BRANCHES_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.VENDOR_DASHBOARD_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.VENDOR_TRANSACTIONS_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.VENDORS_CACHE;

@Service
public class PhysicalGoldServiceImpl
        implements PhysicalGoldService {

    private final UserRepository
            userRepository;

    private final VendorRepository
            vendorRepository;

    private final VendorBranchRepository
            vendorBranchRepository;

    private final AddressRepository
            addressRepository;

    private final
    VirtualGoldHoldingRepository
            holdingRepository;

    private final
    PhysicalGoldTransactionRepository
            physicalGoldTransactionRepository;

    private final
    BranchAllocationService
            branchAllocationService;

    private final PaymentService
            paymentService;

    private final
    TransactionHistoryService
            transactionHistoryService;

    private final PhysicalGoldMapper
            physicalGoldMapper;

    public PhysicalGoldServiceImpl(
            UserRepository userRepository,
            VendorRepository vendorRepository,
            VendorBranchRepository
                    vendorBranchRepository,
            AddressRepository
                    addressRepository,
            VirtualGoldHoldingRepository
                    holdingRepository,
            PhysicalGoldTransactionRepository
                    physicalGoldTransactionRepository,
            BranchAllocationService
                    branchAllocationService,
            PaymentService paymentService,
            TransactionHistoryService
                    transactionHistoryService,
            PhysicalGoldMapper physicalGoldMapper
    ) {

        this.userRepository =
                userRepository;

        this.vendorRepository =
                vendorRepository;

        this.vendorBranchRepository =
                vendorBranchRepository;

        this.addressRepository =
                addressRepository;

        this.holdingRepository =
                holdingRepository;

        this.physicalGoldTransactionRepository =
                physicalGoldTransactionRepository;

        this.branchAllocationService =
                branchAllocationService;

        this.paymentService =
                paymentService;

        this.transactionHistoryService =
                transactionHistoryService;

        this.physicalGoldMapper =
                physicalGoldMapper;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = {USER_DASHBOARD_CACHE, USER_TRANSACTIONS_CACHE, USER_PAYMENTS_CACHE, USER_PHYSICAL_GOLD_CACHE}, key = "#request.userId"),
            @CacheEvict(cacheNames = {VENDOR_DASHBOARD_CACHE, VENDOR_BRANCHES_CACHE, VENDOR_TRANSACTIONS_CACHE, VENDORS_CACHE}, key = "#request.vendorId")
    })
    public PhysicalGoldTransaction
    buyPhysicalGold(
            BuyPhysicalGoldRequest request
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

        Address deliveryAddress =
                addressRepository
                        .findById(
                                request.getDeliveryAddressId()
                        )
                        .orElseThrow(
                                () ->
                                        new AddressNotFoundException(
                                                "Delivery address not found"
                                        )
                        );

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                vendor.getVendorId(),
                                deliveryAddress
                                        .getAddressId(),
                                request.getQuantity()
                        );

        BigDecimal totalAmount =
                vendor.getCurrentGoldPrice()
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

        PhysicalGoldTransaction transaction = physicalGoldMapper.toEntity(
                user,
                allocatedBranch,
                deliveryAddress,
                request.getQuantity(),
                LocalDateTime.now()
        );

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

        return physicalGoldTransactionRepository
                .save(transaction);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = {USER_DASHBOARD_CACHE, USER_HOLDINGS_CACHE, USER_TRANSACTIONS_CACHE, USER_PHYSICAL_GOLD_CACHE}, key = "#request.userId"),
            @CacheEvict(cacheNames = {VENDOR_DASHBOARD_CACHE, VENDOR_BRANCHES_CACHE, VENDOR_TRANSACTIONS_CACHE, VENDORS_CACHE}, allEntries = true)
    })
    public PhysicalGoldTransaction
    convertToPhysicalGold(
            ConvertToPhysicalGoldRequest request
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

        Address deliveryAddress =
                addressRepository
                        .findById(
                                request.getDeliveryAddressId()
                        )
                        .orElseThrow(
                                () ->
                                        new AddressNotFoundException(
                                                "Delivery address not found"
                                        )
                        );

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

        VendorBranch allocatedBranch =
                branchAllocationService
                        .allocateBranch(
                                holding.getBranch()
                                        .getVendor()
                                        .getVendorId(),
                                deliveryAddress
                                        .getAddressId(),
                                request.getQuantity()
                        );

        allocatedBranch.setQuantity(
                allocatedBranch.getQuantity()
                        .subtract(
                                request.getQuantity()
                        )
        );

        holding.setQuantity(
                holding.getQuantity()
                        .subtract(
                                request.getQuantity()
                        )
        );

        BigDecimal totalAmount =
                allocatedBranch
                        .getVendor()
                        .getCurrentGoldPrice()
                        .multiply(
                                request.getQuantity()
                        );

        PhysicalGoldTransaction transaction = physicalGoldMapper.toEntity(
                user,
                allocatedBranch,
                deliveryAddress,
                request.getQuantity(),
                LocalDateTime.now()
        );

        transactionHistoryService
                .createConvertToPhysicalTransaction(
                        user,
                        allocatedBranch,
                        request.getQuantity(),
                        totalAmount,
                        TransactionConstants.SUCCESS
                );

        vendorBranchRepository
                .save(allocatedBranch);

        if (
                holding.getQuantity()
                        .compareTo(BigDecimal.ZERO)
                        == 0
        ) {

            holdingRepository.delete(
                    holding
            );
        }

        else {

            holdingRepository.save(
                    holding
            );
        }

        return physicalGoldTransactionRepository
                .save(transaction);
    }
}
