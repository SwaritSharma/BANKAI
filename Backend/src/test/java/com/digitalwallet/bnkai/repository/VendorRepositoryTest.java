package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Vendor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VendorRepositoryTest {

    @Autowired
    private VendorRepository vendorRepository;

    private Vendor createVendor(
            String vendorName,
            BigDecimal totalGoldQuantity,
            BigDecimal currentGoldPrice
    ) {

        Vendor vendor = new Vendor();

        vendor.setVendorName(vendorName);

        vendor.setDescription("Gold Vendor");

        vendor.setContactPersonName("Rahul");

        vendor.setContactEmail(
                vendorName + "@gmail.com"
        );

        vendor.setContactPhone("9999999999");

        vendor.setWebsiteUrl(
                "https://www.testvendor.com"
        );

        vendor.setTotalGoldQuantity(
                totalGoldQuantity
        );

        vendor.setCurrentGoldPrice(
                currentGoldPrice
        );

        vendor.setCreatedAt(
                LocalDateTime.now()
        );

        vendor.setPassword("password123");

        return vendorRepository.save(vendor);
    }

    @Test
    @DisplayName(
            "Should Return True When Vendor Name Exists"
    )
    void shouldReturnTrueWhenVendorNameExists() {

        createVendor(
                "Tanishq",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(6200)
        );

        boolean exists =
                vendorRepository
                        .existsByVendorName(
                                "Tanishq"
                        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName(
            "Should Return False When Vendor Name Does Not Exist"
    )
    void shouldReturnFalseWhenVendorNameDoesNotExist() {

        boolean exists =
                vendorRepository
                        .existsByVendorName(
                                "InvalidVendor"
                        );

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName(
            "Should Validate Unique Vendor Name"
    )
    void shouldValidateUniqueVendorName() {

        createVendor(
                "Malabar",
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(6150)
        );

        boolean exists =
                vendorRepository
                        .existsByVendorName(
                                "Malabar"
                        );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName(
            "Should Work With Multiple Vendors"
    )
    void shouldWorkWithMultipleVendors() {

        createVendor(
                "Vendor1",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(6100)
        );

        createVendor(
                "Vendor2",
                BigDecimal.valueOf(7000),
                BigDecimal.valueOf(6200)
        );

        assertThat(
                vendorRepository
                        .existsByVendorName(
                                "Vendor1"
                        )
        ).isTrue();

        assertThat(
                vendorRepository
                        .existsByVendorName(
                                "Vendor2"
                        )
        ).isTrue();
    }

    @Test
    @DisplayName(
            "Should Find Vendor By Vendor Name"
    )
    void shouldFindVendorByVendorName() {

        createVendor(
                "Kalyan",
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(6180)
        );

        Optional<Vendor> foundVendor =
                vendorRepository
                        .findByVendorName(
                                "Kalyan"
                        );

        assertThat(foundVendor).isPresent();

        assertThat(
                foundVendor.get()
                        .getVendorName()
        ).isEqualTo("Kalyan");
    }

    @Test
    @DisplayName(
            "Should Return Empty When Vendor Not Found"
    )
    void shouldReturnEmptyWhenVendorNotFound() {

        Optional<Vendor> foundVendor =
                vendorRepository
                        .findByVendorName(
                                "UnknownVendor"
                        );

        assertThat(foundVendor).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Fetch Current Gold Price Correctly"
    )
    void shouldFetchCurrentGoldPriceCorrectly() {

        createVendor(
                "Tanishq",
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(6250)
        );

        Optional<Vendor> foundVendor =
                vendorRepository
                        .findByVendorName(
                                "Tanishq"
                        );

        assertThat(foundVendor).isPresent();

        assertThat(
                foundVendor.get()
                        .getCurrentGoldPrice()
        ).isEqualTo(
                BigDecimal.valueOf(6250)
        );
    }

    @Test
    @DisplayName(
            "Should Fetch Total Gold Quantity Correctly"
    )
    void shouldFetchTotalGoldQuantityCorrectly() {

        createVendor(
                "Malabar",
                BigDecimal.valueOf(12000),
                BigDecimal.valueOf(6100)
        );

        Optional<Vendor> foundVendor =
                vendorRepository
                        .findByVendorName(
                                "Malabar"
                        );

        assertThat(foundVendor).isPresent();

        assertThat(
                foundVendor.get()
                        .getTotalGoldQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(12000)
        );
    }

    @Test
    @DisplayName(
            "Should Fetch Vendor Contact Details"
    )
    void shouldFetchVendorContactDetails() {

        createVendor(
                "Kalyan",
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(6190)
        );

        Optional<Vendor> foundVendor =
                vendorRepository
                        .findByVendorName(
                                "Kalyan"
                        );

        assertThat(foundVendor).isPresent();

        assertThat(
                foundVendor.get()
                        .getContactPhone()
        ).isEqualTo("9999999999");
    }
}