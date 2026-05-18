package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PhysicalGoldTransactionRepositoryTest {

    @Autowired
    private PhysicalGoldTransactionRepository
            physicalGoldTransactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private VendorBranchRepository
            vendorBranchRepository;

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

    private PhysicalGoldTransaction
    createPhysicalTransaction(
            User user,
            VendorBranch branch,
            Address deliveryAddress,
            BigDecimal quantity,
            LocalDateTime createdAt
    ) {

        PhysicalGoldTransaction transaction =
                new PhysicalGoldTransaction();

        transaction.setUser(user);

        transaction.setBranch(branch);

        transaction.setDeliveryAddress(
                deliveryAddress
        );

        transaction.setQuantity(quantity);

        transaction.setCreatedAt(createdAt);

        return physicalGoldTransactionRepository
                .save(transaction);
    }

    @Test
    @DisplayName(
            "Should Find Physical Transactions By User Id"
    )
    void shouldFindPhysicalTransactionsByUserId() {

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

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(5),
                LocalDateTime.now()
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserId(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
        ).hasSize(1);
    }

    @Test
    @DisplayName(
            "Should Find Physical Transactions By User And Branch"
    )
    void shouldFindPhysicalTransactionsByUserAndBranch() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user =
                createUser(
                        "Aman",
                        "aman@gmail.com",
                        BigDecimal.valueOf(70000),
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

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(3),
                LocalDateTime.now()
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserIdAndBranchBranchId(
                                user.getUserId(),
                                branch.getBranchId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
        ).hasSize(1);

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getBranch()
                        .getBranchId()
        ).isEqualTo(
                branch.getBranchId()
        );
    }

    @Test
    @DisplayName(
            "Should Sort Physical Transactions By Created At Desc"
    )
    void shouldSortPhysicalTransactionsByCreatedAtDesc() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Rohit",
                        "rohit@gmail.com",
                        BigDecimal.valueOf(90000),
                        address
                );

        Vendor vendor =
                createVendor("Kalyan");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(1),
                LocalDateTime.of(
                        2025,
                        1,
                        1,
                        10,
                        0
                )
        );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(5),
                LocalDateTime.of(
                        2025,
                        1,
                        2,
                        10,
                        0
                )
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserIdOrderByCreatedAtDesc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(5)
        );
    }

    @Test
    @DisplayName(
            "Should Sort Physical Transactions By Created At Asc"
    )
    void shouldSortPhysicalTransactionsByCreatedAtAsc() {

        Address address =
                createAddress(
                        "Chandigarh",
                        "160017"
                );

        User user =
                createUser(
                        "Test",
                        "test@gmail.com",
                        BigDecimal.valueOf(60000),
                        address
                );

        Vendor vendor =
                createVendor("Tanishq");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(8),
                LocalDateTime.of(
                        2025,
                        1,
                        2,
                        10,
                        0
                )
        );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(2),
                LocalDateTime.of(
                        2025,
                        1,
                        1,
                        10,
                        0
                )
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserIdOrderByCreatedAtAsc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(2)
        );
    }

    @Test
    @DisplayName(
            "Should Sort Physical Transactions By Quantity Desc"
    )
    void shouldSortPhysicalTransactionsByQuantityDesc() {

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
                createVendor("Malabar");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(5000)
                );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(1),
                LocalDateTime.now()
        );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(10),
                LocalDateTime.now()
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserIdOrderByQuantityDesc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(10)
        );
    }

    @Test
    @DisplayName(
            "Should Sort Physical Transactions By Quantity Asc"
    )
    void shouldSortPhysicalTransactionsByQuantityAsc() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user =
                createUser(
                        "Aman",
                        "aman@gmail.com",
                        BigDecimal.valueOf(70000),
                        address
                );

        Vendor vendor =
                createVendor("Kalyan");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(12),
                LocalDateTime.now()
        );

        createPhysicalTransaction(
                user,
                branch,
                address,
                BigDecimal.valueOf(3),
                LocalDateTime.now()
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserIdOrderByQuantityAsc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getQuantity()
        ).isEqualTo(
                BigDecimal.valueOf(3)
        );
    }

    @Test
    @DisplayName(
            "Should Return User Specific Physical Transactions Only"
    )
    void shouldReturnUserSpecificPhysicalTransactionsOnly() {

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

        createPhysicalTransaction(
                user1,
                branch,
                address1,
                BigDecimal.valueOf(5),
                LocalDateTime.now()
        );

        createPhysicalTransaction(
                user2,
                branch,
                address2,
                BigDecimal.valueOf(15),
                LocalDateTime.now()
        );

        Page<PhysicalGoldTransaction> transactions =
                physicalGoldTransactionRepository
                        .findByUserUserId(
                                user1.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
        ).hasSize(1);

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getUser()
                        .getName()
        ).isEqualTo("User1");
    }
}