package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.projection.HoldingSummaryProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(
        path = "virtual-gold-holdings",
        excerptProjection =
                HoldingSummaryProjection.class
)
public interface VirtualGoldHoldingRepository
        extends JpaRepository<VirtualGoldHolding, Integer> {

    @Override
    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    Optional<VirtualGoldHolding> findById(Integer holdingId);

    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    Optional<VirtualGoldHolding>
    findByUserUserIdAndBranchBranchId(
            Integer userId,
            Integer branchId
    );

    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    Page<VirtualGoldHolding>
    findByUserUserId(
            Integer userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    Page<VirtualGoldHolding>
    findByUserUserIdOrderByQuantityDesc(
            Integer userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    Page<VirtualGoldHolding>
    findByUserUserIdOrderByQuantityAsc(
            Integer userId,
            Pageable pageable
    );

    boolean existsByBranchBranchId(Integer branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    @Query("select h from VirtualGoldHolding h where h.holdingId = :holdingId")
    Optional<VirtualGoldHolding> findByHoldingIdForUpdate(
            @Param("holdingId") Integer holdingId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"user", "branch", "branch.vendor", "branch.address"})
    @Query("select h from VirtualGoldHolding h where h.user.userId = :userId and h.branch.branchId = :branchId")
    Optional<VirtualGoldHolding> findByUserUserIdAndBranchBranchIdForUpdate(
            @Param("userId") Integer userId,
            @Param("branchId") Integer branchId
    );
}