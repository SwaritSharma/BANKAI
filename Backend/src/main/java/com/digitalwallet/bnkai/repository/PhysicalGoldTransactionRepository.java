package com.digitalwallet.bnkai.repository;


import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.projection.PhysicalGoldSummaryProjection;
import org.springframework.data.domain.Page;import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(
        path = "physical-gold-transactions",
        excerptProjection =
                PhysicalGoldSummaryProjection.class
)
public interface PhysicalGoldTransactionRepository
        extends JpaRepository<PhysicalGoldTransaction, Integer> {

    Page<PhysicalGoldTransaction>
    findByUserUserId(
            Integer userId,
            Pageable pageable
    );

    Page<PhysicalGoldTransaction>
    findByUserUserIdOrderByCreatedAtDesc(
            Integer userId,
            Pageable pageable
    );

    Page<PhysicalGoldTransaction>
    findByUserUserIdOrderByCreatedAtAsc(
            Integer userId,
            Pageable pageable
    );

    Page<PhysicalGoldTransaction>
    findByUserUserIdOrderByQuantityDesc(
            Integer userId,
            Pageable pageable
    );

    Page<PhysicalGoldTransaction>
    findByUserUserIdOrderByQuantityAsc(
            Integer userId,
            Pageable pageable
    );
}