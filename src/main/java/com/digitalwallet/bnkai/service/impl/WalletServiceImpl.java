package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.dto.UserDTO;
import com.digitalwallet.bnkai.dto.WalletTopupRequest;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.exception.InvalidQuantityException;
import com.digitalwallet.bnkai.exception.UserNotFoundException;
import com.digitalwallet.bnkai.mapper.UserMapper;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.service.PaymentService;
import com.digitalwallet.bnkai.service.WalletService;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_DASHBOARD_CACHE;
import static com.digitalwallet.bnkai.config.RedisCacheConfig.USER_PAYMENTS_CACHE;

@Service
public class WalletServiceImpl
        implements WalletService {

    private final UserRepository
            userRepository;

    private final PaymentService
            paymentService;

    private final UserMapper
            userMapper;

    public WalletServiceImpl(
            UserRepository userRepository,
            PaymentService paymentService,
            UserMapper userMapper
    ) {

        this.userRepository =
                userRepository;

        this.paymentService =
                paymentService;

        this.userMapper =
                userMapper;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = {
            USER_DASHBOARD_CACHE,
            USER_PAYMENTS_CACHE
    }, key = "#request.userId")
    public UserDTO topupWallet(
            WalletTopupRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        if (
                request.getAmount()
                        .compareTo(BigDecimal.ZERO)
                        <= 0
        ) {

            throw new InvalidQuantityException(
                    "Amount must be greater than 0"
            );
        }

        User user =
                userRepository
                        .findByUserIdForUpdate(
                                request.getUserId()
                        )
                        .orElseThrow(
                                () ->
                                        new UserNotFoundException(
                                                "User not found"
                                        )
                        );

        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !user.getEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Access denied");
        }

        BigDecimal currentBalance =
                user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;

        BigDecimal updatedBalance =
                currentBalance.add(
                        request.getAmount()
                );

        user.setBalance(
                updatedBalance
        );

        paymentService
                .createWalletCreditEntry(
                        user,
                        request.getAmount(),
                        request.getPaymentMethod(),
                        PaymentConstants.SUCCESS
                );

        User savedUser = userRepository
                .save(user);

        return userMapper.toDto(savedUser);
    }
}
