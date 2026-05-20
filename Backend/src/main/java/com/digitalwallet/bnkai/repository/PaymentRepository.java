package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Payment;
import com.digitalwallet.bnkai.projection.PaymentSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
        path = "payments",
        excerptProjection =
                PaymentSummaryProjection.class
)
public interface PaymentRepository
        extends JpaRepository<Payment, Integer> {

    Page<Payment>
    findByUserUserId(
            Integer userId,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdAndPaymentMethod(
            Integer userId,
            String paymentMethod,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdAndTransactionType(
            Integer userId,
            String transactionType,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdAndPaymentStatus(
            Integer userId,
            String paymentStatus,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdOrderByCreatedAtDesc(
            Integer userId,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdOrderByCreatedAtAsc(
            Integer userId,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdOrderByAmountDesc(
            Integer userId,
            Pageable pageable
    );

    Page<Payment>
    findByUserUserIdOrderByAmountAsc(
            Integer userId,
            Pageable pageable
    );
}