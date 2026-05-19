package com.personal.project.repository;

import com.personal.project.entity.Address;
import com.personal.project.entity.Vendor;
import com.personal.project.entity.VendorBranch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VendorBranchRepositoryTest {

    @Autowired
    private VendorBranchRepository vendorBranchRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Address createAddress(
            String city,
            String state,
            String postalCode,
            String country
    ) {

        Address address = new Address();

        address.setStreet("Sector 17");
        address.setCity(city);
        address.setState(state);
        address.setPostalCode(postalCode);
        address.setCountry(country);

        return addressRepository.save(address);
    }

    private Vendor createVendor(
            String vendorName
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
                BigDecimal.valueOf(10000)
        );

        vendor.setCurrentGoldPrice(
                BigDecimal.valueOf(6200)
        );

        vendor.setCreatedAt(
                LocalDateTime.now()
        );

        vendor.setPassword("password123");

        return vendorRepository.save(vendor);
    }

    private VendorBranch createBranch(
            Vendor vendor,
            Address address,
            BigDecimal quantity
    ) {

        VendorBranch branch =
                new VendorBranch();

        branch.setVendor(vendor);

        branch.setAddress(address);

        branch.setQuantity(quantity);

        branch.setCreatedAt(
                LocalDateTime.now()
        );

        return vendorBranchRepository
                .save(branch);
    }

    @Test
    @DisplayName(
            "Should Find All Branches By Vendor Id"
    )
    void shouldFindAllBranchesByVendorId() {

        Vendor vendor =
                createVendor("Tanishq");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(1000)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(2000)
        );

        List<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorId(
                                vendor.getVendorId()
                        );

        assertThat(branches)
                .hasSize(2);
    }

    @Test
    @DisplayName(
            "Should Return Empty When Vendor Branches Not Found"
    )
    void shouldReturnEmptyWhenVendorBranchesNotFound() {

        List<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorId(
                                999
                        );

        assertThat(branches).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Count Vendor Branches Correctly"
    )
    void shouldCountVendorBranchesCorrectly() {

        Vendor vendor =
                createVendor("Malabar");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(1000)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(2000)
        );

        long count =
                vendorBranchRepository
                        .countByVendorVendorId(
                                vendor.getVendorId()
                        );

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName(
            "Should Filter Branches By State"
    )
    void shouldFilterBranchesByState() {

        Vendor vendor =
                createVendor("Kalyan");

        Address punjabAddress =
                createAddress(
                        "Chandigarh",
                        "Punjab",
                        "160017",
                        "India"
                );

        Address delhiAddress =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        createBranch(
                vendor,
                punjabAddress,
                BigDecimal.valueOf(1000)
        );

        createBranch(
                vendor,
                delhiAddress,
                BigDecimal.valueOf(2000)
        );

        Page<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorIdAndAddressState(
                                vendor.getVendorId(),
                                "Punjab",
                                PageRequest.of(0, 10)
                        );

        assertThat(
                branches.getContent()
        ).hasSize(1);

        assertThat(
                branches.getContent()
                        .get(0)
                        .getAddress()
                        .getState()
        ).isEqualTo("Punjab");
    }

    @Test
    @DisplayName(
            "Should Filter Branches By Country"
    )
    void shouldFilterBranchesByCountry() {

        Vendor vendor =
                createVendor("Tanishq");

        Address indiaAddress =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address usaAddress =
                createAddress(
                        "New York",
                        "NY",
                        "10001",
                        "USA"
                );

        createBranch(
                vendor,
                indiaAddress,
                BigDecimal.valueOf(1000)
        );

        createBranch(
                vendor,
                usaAddress,
                BigDecimal.valueOf(1500)
        );

        Page<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorIdAndAddressCountry(
                                vendor.getVendorId(),
                                "India",
                                PageRequest.of(0, 10)
                        );

        assertThat(
                branches.getContent()
        ).hasSize(1);

        assertThat(
                branches.getContent()
                        .get(0)
                        .getAddress()
                        .getCountry()
        ).isEqualTo("India");
    }

    @Test
    @DisplayName(
            "Should Sort Branches By Quantity Ascending"
    )
    void shouldSortBranchesByQuantityAscending() {

        Vendor vendor =
                createVendor("Malabar");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(500)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(2000)
        );

        Page<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorIdOrderByQuantityAsc(
                                vendor.getVendorId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                branches.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(500)
        );
    }

    @Test
    @DisplayName(
            "Should Sort Branches By Quantity Descending"
    )
    void shouldSortBranchesByQuantityDescending() {

        Vendor vendor =
                createVendor("Kalyan");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(500)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(2500)
        );

        Page<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorIdOrderByQuantityDesc(
                                vendor.getVendorId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                branches.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(2500)
        );
    }

    @Test
    @DisplayName(
            "Should Find Branches Having Sufficient Quantity"
    )
    void shouldFindBranchesHavingSufficientQuantity() {

        Vendor vendor =
                createVendor("Tanishq");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(500)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(3000)
        );

        List<VendorBranch> branches =
                vendorBranchRepository
                        .findByVendorVendorIdAndQuantityGreaterThanEqual(
                                vendor.getVendorId(),
                                BigDecimal.valueOf(1000)
                        );

        assertThat(branches)
                .hasSize(1);

        assertThat(
                branches.get(0)
                        .getQuantity()
        ).isGreaterThanOrEqualTo(
                BigDecimal.valueOf(1000)
        );
    }

    @Test
    @DisplayName(
            "Should Find Branch By Postal Code And Quantity"
    )
    void shouldFindBranchByPostalCodeAndQuantity() {

        Vendor vendor =
                createVendor("Malabar");

        Address address =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        createBranch(
                vendor,
                address,
                BigDecimal.valueOf(5000)
        );

        Optional<VendorBranch> branch =
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                vendor.getVendorId(),
                                "110001",
                                BigDecimal.valueOf(1000)
                        );

        assertThat(branch).isPresent();

        assertThat(
                branch.get()
                        .getAddress()
                        .getPostalCode()
        ).isEqualTo("110001");
    }

    @Test
    @DisplayName(
            "Should Return Empty When Postal Code Does Not Match"
    )
    void shouldReturnEmptyWhenPostalCodeDoesNotMatch() {

        Vendor vendor =
                createVendor("Kalyan");

        Address address =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        createBranch(
                vendor,
                address,
                BigDecimal.valueOf(5000)
        );

        Optional<VendorBranch> branch =
                vendorBranchRepository
                        .findFirstByVendorVendorIdAndAddressPostalCodeAndQuantityGreaterThanEqual(
                                vendor.getVendorId(),
                                "999999",
                                BigDecimal.valueOf(1000)
                        );

        assertThat(branch).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Find Lowest Quantity Branch"
    )
    void shouldFindLowestQuantityBranch() {

        Vendor vendor =
                createVendor("Tanishq");

        Address address1 =
                createAddress(
                        "Delhi",
                        "Delhi",
                        "110001",
                        "India"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "Maharashtra",
                        "400001",
                        "India"
                );

        createBranch(
                vendor,
                address1,
                BigDecimal.valueOf(500)
        );

        createBranch(
                vendor,
                address2,
                BigDecimal.valueOf(5000)
        );

        Optional<VendorBranch> branch =
                vendorBranchRepository
                        .findFirstByVendorVendorIdOrderByQuantityAsc(
                                vendor.getVendorId()
                        );

        assertThat(branch).isPresent();

        assertThat(
                branch.get()
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(500)
        );
    }
}