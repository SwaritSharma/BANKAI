package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.projection.VendorDashboardProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.Optional;

@RepositoryRestResource(
        path = "vendors",
        excerptProjection =
                VendorDashboardProjection.class
)
public interface VendorRepository
        extends JpaRepository<Vendor, Integer> {

    boolean existsByVendorName(
            String vendorName
    );

    Optional<Vendor> findByVendorName(
            String vendorName
    );

    Optional<Vendor> findByContactEmail(
            String contactEmail
    );
}