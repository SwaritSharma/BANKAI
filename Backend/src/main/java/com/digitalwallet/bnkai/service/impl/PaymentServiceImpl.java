package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.entity.Payment;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.mapper.PaymentMapper;
import com.digitalwallet.bnkai.repository.PaymentRepository;
import com.digitalwallet.bnkai.service.PaymentService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl
        implements PaymentService {

    private final PaymentRepository
            paymentRepository;

    private final PaymentMapper
            paymentMapper;

    public PaymentServiceImpl(
            PaymentRepository paymentRepository,
            PaymentMapper paymentMapper
    ) {

        this.paymentRepository =
                paymentRepository;

        this.paymentMapper =
                paymentMapper;
    }

    @Override
    public Payment createWalletCreditEntry(
            User user,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    ) {

        Payment payment = paymentMapper.toEntity(
                user,
                amount,
                paymentMethod,
                PaymentConstants.CREDITED_TO_WALLET,
                paymentStatus,
                LocalDateTime.now()
        );

        return paymentRepository
                .save(payment);
    }

    @Override
    public Payment createWalletDebitEntry(
            User user,
            BigDecimal amount,
            String paymentMethod,
            String paymentStatus
    ) {

        Payment payment = paymentMapper.toEntity(
                user,
                amount,
                paymentMethod,
                PaymentConstants.DEBITED_FROM_WALLET,
                paymentStatus,
                LocalDateTime.now()
        );

        return paymentRepository
                .save(payment);
    }
}
