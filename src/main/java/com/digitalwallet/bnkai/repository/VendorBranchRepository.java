package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.projection.BranchSummaryProjection;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(
        path = "vendor-branches",
        excerptProjection =
                BranchSummaryProjection.class
)
public interface VendorBranchRepository
        extends JpaRepository<VendorBranch, Integer> {

    List<VendorBranch>
    findByVendorVendorId(
            Integer vendorId
    );

    long countByVendorVendorId(
            Integer vendorId
    );

    Page<VendorBranch>
    findByVendorVendorIdAndAddressState(
            Integer vendorId,
            String state,
            Pageable pageable
    );

    Page<VendorBranch>
    findByVendorVendorIdAndAddressCountry(
            Integer vendorId,
            String country,
            Pageable pageable
    );

    Page<VendorBranch>
    findByVendorVendorIdOrderByQuantityAsc(
            Integer vendorId,
            Pageable pageable
    );

    Page<VendorBranch>
    findByVendorVendorIdOrderByQuantityDesc(
            Integer vendorId,
            Pageable pageable
    );

    List<VendorBranch>
    findByVendorVendorIdAndQuantityGreaterThanEqual(
            Integer vendorId,
            BigDecimal quantity
    );

    Optional<VendorBranch>
    findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
            Integer vendorId,
            String postalCode,
            BigDecimal quantity
    );

    Optional<VendorBranch>
    findFirstByVendorVendorIdOrderByQuantityAsc(
            Integer vendorId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from VendorBranch b where b.branchId = :branchId")
    Optional<VendorBranch> findByBranchIdForUpdate(
            @Param("branchId") Integer branchId
    );
}