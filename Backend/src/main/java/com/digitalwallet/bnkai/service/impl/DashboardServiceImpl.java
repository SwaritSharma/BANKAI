package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.dto.*;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.exception.DuplicateResourceException;
import com.digitalwallet.bnkai.exception.UserNotFoundException;
import com.digitalwallet.bnkai.mapper.*;
import com.digitalwallet.bnkai.repository.*;
import com.digitalwallet.bnkai.service.DashboardService;
import com.digitalwallet.bnkai.service.GoldPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.*;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;
    private final VirtualGoldHoldingRepository holdingRepository;
    private final TransactionHistoryRepository transactionRepository;
    private final PhysicalGoldTransactionRepository physicalGoldTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final AddressRepository addressRepository;
    private final GoldPriceService goldPriceService;
    private final DashboardMapper dashboardMapper;
    private final UserMapper userMapper;
    private final AddressMapper addressMapper;
    private final HoldingMapper holdingMapper;
    private final TransactionMapper transactionMapper;
    private final PhysicalGoldMapper physicalGoldMapper;
    private final PaymentMapper paymentMapper;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_DASHBOARD_CACHE, key = "#userId")
    public DashboardDTO getDashboard(Integer userId) {
        User user = validateAndGetUser(userId);
        
        List<VirtualGoldHolding> holdings = holdingRepository.findByUserUserId(userId, PageRequest.of(0, 1000)).getContent();
        
        BigDecimal totalGrams = holdings.stream()
                .map(h -> h.getQuantity() != null ? h.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal currentPrice = goldPriceService.getCurrentPrice().getPrice();
        BigDecimal totalValue = totalGrams.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
        
        // Mock PnL since we don't store average buy price
        BigDecimal pnlAmount = totalValue.multiply(new BigDecimal("0.15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal pnlPercent = new BigDecimal("15.00");
        
        return dashboardMapper.toDashboard(
                user,
                user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO,
                totalGrams,
                totalValue,
                currentPrice,
                pnlAmount,
                pnlPercent
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {
            USER_DASHBOARD_CACHE,
            USER_ADDRESSES_CACHE
    }, key = "#userId")
    public void updateProfile(Integer userId, EditProfileRequest request) {
        User user = validateAndGetUser(userId);
        if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.findByEmail(request.getEmail()).isPresent() || vendorRepository.findByContactEmail(request.getEmail()).isPresent()) {
                throw new DuplicateResourceException("Email already in use");
            }
        }
        userMapper.updateProfile(request, user);

        if (request.getStreet() != null && !request.getStreet().isEmpty()) {
            Address address = user.getAddress();
            if (address == null) {
                address = new Address();
            }
            addressMapper.updateAddressFromProfile(request, address);
            address = addressRepository.save(address);
            user.setAddress(address);
        }

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_HOLDINGS_CACHE, key = "#userId")
    public List<HoldingDTO> getHoldings(Integer userId) {
        validateAndGetUser(userId);
        BigDecimal currentPrice = goldPriceService.getCurrentPrice().getPrice();
        return holdingMapper.toDtoList(
                holdingRepository.findByUserUserId(userId, PageRequest.of(0, 1000)).getContent(),
                currentPrice
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_TRANSACTIONS_CACHE, key = "#userId")
    public List<TransactionDTO> getTransactions(Integer userId) {
        validateAndGetUser(userId);
        return transactionMapper.toDtoList(
                transactionRepository.findByUserUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 100)).getContent()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_ADDRESSES_CACHE, key = "#userId")
    public List<HoldingDTO.AddressDTO> getAddresses(Integer userId) {
        try {
            User user = validateAndGetUser(userId);
            if (user.getAddress() != null) {
                return java.util.Collections.singletonList(addressMapper.toDto(user.getAddress()));
            }
            return new java.util.ArrayList<>();
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return new java.util.ArrayList<>();
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_PHYSICAL_GOLD_CACHE, key = "#userId")
    public List<PhysicalGoldDTO> getPhysicalGold(Integer userId) {
        validateAndGetUser(userId);
        return physicalGoldMapper.toDtoList(
                physicalGoldTransactionRepository.findByUserUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 100)).getContent()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = USER_PAYMENTS_CACHE, key = "#userId")
    public List<PaymentDTO> getPayments(Integer userId) {
        validateAndGetUser(userId);
        return paymentMapper.toDtoList(
                paymentRepository.findByUserUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 100)).getContent()
        );
    }

    private User validateAndGetUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !user.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Access denied");
        }
        return user;
    }
}
