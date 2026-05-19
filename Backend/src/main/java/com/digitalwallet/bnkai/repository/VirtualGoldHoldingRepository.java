package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.projection.HoldingSummaryProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
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
}