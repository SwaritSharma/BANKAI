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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class TransactionHistoryRepositoryTest {

    @Autowired
    private TransactionHistoryRepository
            transactionHistoryRepository;

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

    private TransactionHistory createTransaction(
            User user,
            VendorBranch branch,
            BigDecimal amount,
            BigDecimal quantity,
            String transactionType,
            String transactionStatus,
            LocalDateTime createdAt
    ) {

        TransactionHistory transaction =
                new TransactionHistory();

        transaction.setUser(user);

        transaction.setBranch(branch);

        transaction.setAmount(amount);

        transaction.setQuantity(quantity);

        transaction.setTransactionType(
                transactionType
        );

        transaction.setTransactionStatus(
                transactionStatus
        );

        transaction.setCreatedAt(createdAt);

        return transactionHistoryRepository
                .save(transaction);
    }

    @Test
    @DisplayName(
            "Should Find Transactions By User Id"
    )
    void shouldFindTransactionsByUserId() {

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

        createTransaction(
                user,
                branch,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(2),
                "Buy",
                "Success",
                LocalDateTime.now()
        );

        Page<TransactionHistory> transactions =
                transactionHistoryRepository
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
            "Should Return Latest Five User Transactions"
    )
    void shouldReturnLatestFiveUserTransactions() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        User user =
                createUser(
                        "Aman",
                        "aman@gmail.com",
                        BigDecimal.valueOf(90000),
                        address
                );

        Vendor vendor =
                createVendor("Malabar");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(4000)
                );

        for (int i = 1; i <= 7; i++) {

            createTransaction(
                    user,
                    branch,
                    BigDecimal.valueOf(i * 1000),
                    BigDecimal.valueOf(i),
                    "Buy",
                    "Success",
                    LocalDateTime.now()
                            .plusMinutes(i)
            );
        }

        List<TransactionHistory> transactions =
                transactionHistoryRepository
                        .findTop5ByUserUserIdOrderByCreatedAtDesc(
                                user.getUserId()
                        );

        assertThat(transactions)
                .hasSize(5);
    }

    @Test
    @DisplayName(
            "Should Return Latest Five Vendor Transactions"
    )
    void shouldReturnLatestFiveVendorTransactions() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Rohit",
                        "rohit@gmail.com",
                        BigDecimal.valueOf(70000),
                        address
                );

        Vendor vendor =
                createVendor("Kalyan");

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        for (int i = 1; i <= 7; i++) {

            createTransaction(
                    user,
                    branch,
                    BigDecimal.valueOf(i * 2000),
                    BigDecimal.valueOf(i),
                    "Sell",
                    "Success",
                    LocalDateTime.now()
                            .plusMinutes(i)
            );
        }

        List<TransactionHistory> transactions =
                transactionHistoryRepository
                        .findTop5ByBranchVendorVendorIdOrderByCreatedAtDesc(
                                vendor.getVendorId()
                        );

        assertThat(transactions)
                .hasSize(5);
    }

    @Test
    @DisplayName(
            "Should Filter Transactions By Transaction Type"
    )
    void shouldFilterTransactionsByTransactionType() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        User user =
                createUser(
                        "Test",
                        "test@gmail.com",
                        BigDecimal.valueOf(50000),
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

        createTransaction(
                user,
                branch,
                BigDecimal.valueOf(10000),
                BigDecimal.ONE,
                "Buy",
                "Success",
                LocalDateTime.now()
        );

        createTransaction(
                user,
                branch,
                BigDecimal.valueOf(5000),
                BigDecimal.ONE,
                "Sell",
                "Success",
                LocalDateTime.now()
        );

        Page<TransactionHistory> transactions =
                transactionHistoryRepository
                        .findByUserUserIdAndTransactionType(
                                user.getUserId(),
                                "Buy",
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
        ).hasSize(1);

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getTransactionType()
        ).isEqualTo("Buy");
    }

    @Test
    @DisplayName(
            "Should Sort Transactions By Amount Desc"
    )
    void shouldSortTransactionsByAmountDesc() {

        Address address =
                createAddress(
                        "Chandigarh",
                        "160017"
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

        VendorBranch branch =
                createBranch(
                        vendor,
                        address,
                        BigDecimal.valueOf(3000)
                );

        createTransaction(
                user,
                branch,
                BigDecimal.valueOf(5000),
                BigDecimal.ONE,
                "Buy",
                "Success",
                LocalDateTime.now()
        );

        createTransaction(
                user,
                branch,
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(2),
                "Sell",
                "Success",
                LocalDateTime.now()
        );

        Page<TransactionHistory> transactions =
                transactionHistoryRepository
                        .findByUserUserIdOrderByAmountDesc(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                transactions.getContent()
                        .get(0)
                        .getAmount()
        ).isEqualTo(
                BigDecimal.valueOf(20000)
        );
    }

    @Test
    @DisplayName(
            "Should Return User Specific Transactions Only"
    )
    void shouldReturnUserSpecificTransactionsOnly() {

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

        createTransaction(
                user1,
                branch,
                BigDecimal.valueOf(10000),
                BigDecimal.ONE,
                "Buy",
                "Success",
                LocalDateTime.now()
        );

        createTransaction(
                user2,
                branch,
                BigDecimal.valueOf(15000),
                BigDecimal.ONE,
                "Sell",
                "Success",
                LocalDateTime.now()
        );

        Page<TransactionHistory> transactions =
                transactionHistoryRepository
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