package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.constants.PaymentConstants;
import com.digitalwallet.bnkai.entity.Payment;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.mapper.PaymentMapper;
import com.digitalwallet.bnkai.repository.PaymentRepository;
import com.digitalwallet.bnkai.service.impl.PaymentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PaymentRepository
            paymentRepository;

    @Mock
    private PaymentMapper
            paymentMapper;

    @InjectMocks
    private PaymentServiceImpl
            paymentService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setUserId(1);
    }

    @Test
    void createWalletCreditEntry_ShouldCreateSuccessfully() {

        stubPaymentMapper();

        when(
                paymentRepository.save(
                        any(Payment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        Payment payment =
                paymentService
                        .createWalletCreditEntry(
                                user,
                                new BigDecimal("5000"),
                                PaymentConstants.BANK_TRANSFER,
                                PaymentConstants.SUCCESS
                        );

        assertNotNull(payment);

        assertEquals(
                user,
                payment.getUser()
        );

        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(
                                payment.getAmount()
                        )
        );

        assertEquals(
                PaymentConstants.BANK_TRANSFER,
                payment.getPaymentMethod()
        );

        assertEquals(
                PaymentConstants.CREDITED_TO_WALLET,
                payment.getTransactionType()
        );

        assertEquals(
                PaymentConstants.SUCCESS,
                payment.getPaymentStatus()
        );

        assertNotNull(
                payment.getCreatedAt()
        );

        verify(paymentRepository)
                .save(
                        any(Payment.class)
                );
        verify(paymentMapper).toEntity(
                eq(user),
                eq(new BigDecimal("5000")),
                eq(PaymentConstants.BANK_TRANSFER),
                eq(PaymentConstants.CREDITED_TO_WALLET),
                eq(PaymentConstants.SUCCESS),
                any()
        );
    }

    @Test
    void createWalletDebitEntry_ShouldCreateSuccessfully() {

        stubPaymentMapper();

        when(
                paymentRepository.save(
                        any(Payment.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        Payment payment =
                paymentService
                        .createWalletDebitEntry(
                                user,
                                new BigDecimal("3000"),
                                PaymentConstants.BANK_TRANSFER,
                                PaymentConstants.SUCCESS
                        );

        assertNotNull(payment);

        assertEquals(
                user,
                payment.getUser()
        );

        assertEquals(
                0,
                new BigDecimal("3000")
                        .compareTo(
                                payment.getAmount()
                        )
        );

        assertEquals(
                PaymentConstants.BANK_TRANSFER,
                payment.getPaymentMethod()
        );

        assertEquals(
                PaymentConstants.DEBITED_FROM_WALLET,
                payment.getTransactionType()
        );

        assertEquals(
                PaymentConstants.SUCCESS,
                payment.getPaymentStatus()
        );

        assertNotNull(
                payment.getCreatedAt()
        );

        verify(paymentRepository)
                .save(
                        any(Payment.class)
                );
        verify(paymentMapper).toEntity(
                eq(user),
                eq(new BigDecimal("3000")),
                eq(PaymentConstants.BANK_TRANSFER),
                eq(PaymentConstants.DEBITED_FROM_WALLET),
                eq(PaymentConstants.SUCCESS),
                any()
        );
    }

    private void stubPaymentMapper() {
        when(
                paymentMapper.toEntity(
                        any(User.class),
                        any(BigDecimal.class),
                        anyString(),
                        anyString(),
                        anyString(),
                        any()
                )
        ).thenAnswer(invocation -> new Payment(
                null,
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3),
                invocation.getArgument(4),
                invocation.getArgument(5)
        ));
    }
}
