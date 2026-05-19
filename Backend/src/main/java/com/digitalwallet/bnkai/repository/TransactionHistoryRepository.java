package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.projection.TransactionSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.math.BigDecimal;
import java.util.List;

@RepositoryRestResource(
        path = "transaction-history",
        excerptProjection =
                TransactionSummaryProjection.class
)
public interface TransactionHistoryRepository
        extends JpaRepository<TransactionHistory, Integer> {

    @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM TransactionHistory t WHERE t.branch.vendor.vendorId = :vendorId AND t.transactionType = 'Buy' AND t.transactionStatus = 'Success'")
    BigDecimal sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(
            @Param("vendorId") Integer vendorId
    );

    Page<TransactionHistory>
    findByUserUserId(
            Integer userId,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdAndTransactionType(
            Integer userId,
            String transactionType,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdAndTransactionStatus(
            Integer userId,
            String transactionStatus,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdOrderByCreatedAtDesc(
            Integer userId,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdOrderByCreatedAtAsc(
            Integer userId,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdOrderByAmountDesc(
            Integer userId,
            Pageable pageable
    );

    Page<TransactionHistory>
    findByUserUserIdOrderByAmountAsc(
            Integer userId,
            Pageable pageable
    );

    List<TransactionHistory>
    findTop5ByUserUserIdOrderByCreatedAtDesc(
            Integer userId
    );

    List<TransactionHistory>
    findTop5ByBranchVendorVendorIdOrderByCreatedAtDesc(
            Integer vendorId
    );

    Page<TransactionHistory>
    findByBranchVendorVendorIdOrderByCreatedAtDesc(
            Integer vendorId,
            Pageable pageable
    );

    boolean existsByBranchBranchId(Integer branchId);
}