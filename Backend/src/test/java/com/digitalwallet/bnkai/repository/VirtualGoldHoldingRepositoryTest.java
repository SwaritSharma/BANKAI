package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class VirtualGoldHoldingRepositoryTest {

    @Autowired
    private VirtualGoldHoldingRepository
            virtualGoldHoldingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorBranchRepository vendorBranchRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Address createAddress(
            String city,
            String postalCode
    ) {

        Address address = new Address();

        address.setStreet("Sector 17");

        address.setCity(city);

        address.setState("Punjab");

        address.setPostalCode(postalCode);

        address.setCountry("India");

        return addressRepository.save(address);
    }

    private User createUser(
            String name,
            String email,
            BigDecimal balance,
            Address address
    ) {

        User user = new User();

        user.setName(name);

        user.setEmail(email);

        user.setPassword("password123");

        user.setBalance(balance);

        user.setCreatedAt(
                LocalDateTime.now()
        );

        user.setAddress(address);

        return userRepository.save(user);
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

    private VirtualGoldHolding createHolding(
            User user,
            VendorBranch branch,
            BigDecimal quantity
    ) {

        VirtualGoldHolding holding =
                new VirtualGoldHolding();

        holding.setUser(user);

        holding.setBranch(branch);

        holding.setQuantity(quantity);

        holding.setCreatedAt(
                LocalDateTime.now()
        );

        return virtualGoldHoldingRepository
                .save(holding);
    }

    @Test
    @DisplayName(
            "Should Find Holding By User And Branch"
    )
    void shouldFindHoldingByUserAndBranch() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Friday",
                        "friday@gmail.com",
                        BigDecimal.valueOf(100000),
                        address
                );

        Vendor vendor =
                createVendor("Tanishq");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(5000)
                );

        createHolding(
                user,
                branch,
                BigDecimal.valueOf(5)
        );

        Optional<VirtualGoldHolding> holding =
                virtualGoldHoldingRepository
                        .findByUserUserIdAndBranchBranchId(
                                user.getUserId(),
                                branch.getBranchId()
                        );

        assertThat(holding).isPresent();

        assertThat(
                holding.get()
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(5)
        );
    }

    @Test
    @DisplayName(
            "Should Return Empty When Holding Not Found"
    )
    void shouldReturnEmptyWhenHoldingNotFound() {

        Optional<VirtualGoldHolding> holding =
                virtualGoldHoldingRepository
                        .findByUserUserIdAndBranchBranchId(
                                999,
                                999
                        );

        assertThat(holding).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Validate User Relationship"
    )
    void shouldValidateUserRelationship() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user =
                createUser(
                        "Aman",
                        "aman@gmail.com",
                        BigDecimal.valueOf(50000),
                        address
                );

        Vendor vendor =
                createVendor("Malabar");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        createHolding(
                user,
                branch,
                BigDecimal.valueOf(10)
        );

        Optional<VirtualGoldHolding> holding =
                virtualGoldHoldingRepository
                        .findByUserUserIdAndBranchBranchId(
                                user.getUserId(),
                                branch.getBranchId()
                        );

        assertThat(holding).isPresent();

        assertThat(
                holding.get()
                        .getUser()
                        .getName()
        ).isEqualTo("Aman");
    }

    @Test
    @DisplayName(
            "Should Validate Branch Relationship"
    )
    void shouldValidateBranchRelationship() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Rohit",
                        "rohit@gmail.com",
                        BigDecimal.valueOf(60000),
                        address
                );

        Vendor vendor =
                createVendor("Kalyan");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(2000)
                );

        createHolding(
                user,
                branch,
                BigDecimal.valueOf(8)
        );

        Optional<VirtualGoldHolding> holding =
                virtualGoldHoldingRepository
                        .findByUserUserIdAndBranchBranchId(
                                user.getUserId(),
                                branch.getBranchId()
                        );

        assertThat(holding).isPresent();

        assertThat(
                holding.get()
                        .getBranch()
                        .getBranchId()
        ).isEqualTo(
                branch.getBranchId()
        );
    }

    @Test
    @DisplayName(
            "Should Fetch Holdings By User Id"
    )
    void shouldFetchHoldingsByUserId() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Friday",
                        "friday@gmail.com",
                        BigDecimal.valueOf(90000),
                        address
                );

        Vendor vendor =
                createVendor("Tanishq");

        VendorBranch branch1 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(5000)
                );

        VendorBranch branch2 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        createHolding(
                user,
                branch1,
                BigDecimal.valueOf(5)
        );

        createHolding(
                user,
                branch2,
                BigDecimal.valueOf(10)
        );

        Page<VirtualGoldHolding> holdings =
                virtualGoldHoldingRepository
                        .findByUserUserId(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                holdings.getContent()
        ).hasSize(2);
    }

    @Test
    @DisplayName(
            "Should Return Empty Holdings When User Has None"
    )
    void shouldReturnEmptyHoldingsWhenUserHasNone() {

        Address address =
                createAddress(
                        "Pune",
                        "411001"
                );

        User user =
                createUser(
                        "Test",
                        "test@gmail.com",
                        BigDecimal.valueOf(10000),
                        address
                );

        Page<VirtualGoldHolding> holdings =
                virtualGoldHoldingRepository
                        .findByUserUserId(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                holdings.getContent()
        ).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Sort Holdings By Quantity Descending"
    )
    void shouldSortHoldingsByQuantityDescending() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Friday",
                        "friday@gmail.com",
                        BigDecimal.valueOf(80000),
                        address
                );

        Vendor vendor =
                createVendor("Malabar");

        VendorBranch branch1 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        VendorBranch branch2 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        createHolding(
                user,
                branch1,
                BigDecimal.valueOf(2)
        );

        createHolding(
                user,
                branch2,
                BigDecimal.valueOf(10)
        );

        Page<VirtualGoldHolding> holdings =
                virtualGoldHoldingRepository
                        .findByUserUserIdOrderByQuantityDesc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                holdings.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(10)
        );
    }

    @Test
    @DisplayName(
            "Should Sort Holdings By Quantity Ascending"
    )
    void shouldSortHoldingsByQuantityAscending() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user =
                createUser(
                        "Aman",
                        "aman@gmail.com",
                        BigDecimal.valueOf(75000),
                        address
                );

        Vendor vendor =
                createVendor("Kalyan");

        VendorBranch branch1 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        VendorBranch branch2 =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        createHolding(
                user,
                branch1,
                BigDecimal.valueOf(12)
        );

        createHolding(
                user,
                branch2,
                BigDecimal.valueOf(3)
        );

        Page<VirtualGoldHolding> holdings =
                virtualGoldHoldingRepository
                        .findByUserUserIdOrderByQuantityAsc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                holdings.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(3)
        );
    }

    @Test
    @DisplayName(
            "Should Return User Specific Holdings Only"
    )
    void shouldReturnUserSpecificHoldingsOnly() {

        Address address1 =
                createAddress(
                        "Delhi",
                        "110001"
                );

        Address address2 =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user1 =
                createUser(
                        "User1",
                        "user1@gmail.com",
                        BigDecimal.valueOf(50000),
                        address1
                );

        User user2 =
                createUser(
                        "User2",
                        "user2@gmail.com",
                        BigDecimal.valueOf(70000),
                        address2
                );

        Vendor vendor =
                createVendor("Tanishq");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address1,
                        BigDecimal.valueOf(5000)
                );

        createHolding(
                user1,
                branch,
                BigDecimal.valueOf(5)
        );

        createHolding(
                user2,
                branch,
                BigDecimal.valueOf(15)
        );

        Page<VirtualGoldHolding> holdings =
                virtualGoldHoldingRepository
                        .findByUserUserId(
                                user1.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                holdings.getContent()
        ).hasSize(1);

        assertThat(
                holdings.getContent()
                        .get(0)
                        .getUser()
                        .getName()
        ).isEqualTo("User1");
    }
}