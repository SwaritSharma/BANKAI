package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.Payment;
import com.digitalwallet.bnkai.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

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

        user.setPassword(
                new BCryptPasswordEncoder()
                        .encode("password123")
        );

        user.setBalance(balance);

        user.setCreatedAt(
                LocalDateTime.now()
        );

        user.setAddress(address);

        return userRepository.save(user);
    }

    private Payment createPayment(
            User user,
            BigDecimal amount,
            String paymentMethod,
            String transactionType,
            String paymentStatus,
            LocalDateTime createdAt
    ) {

        Payment payment = new Payment();

        payment.setUser(user);

        payment.setAmount(amount);

        payment.setPaymentMethod(
                paymentMethod
        );

        payment.setTransactionType(
                transactionType
        );

        payment.setPaymentStatus(
                paymentStatus
        );

        payment.setCreatedAt(createdAt);

        return paymentRepository.save(payment);
    }

    @Test
    @DisplayName(
            "Should Find Payments By User Id"
    )
    void shouldFindPaymentsByUserId() {

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

        createPayment(
                user,
                BigDecimal.valueOf(5000),
                "Google Pay",
                "Credited to wallet",
                "Success",
                LocalDateTime.now()
        );

        createPayment(
                user,
                BigDecimal.valueOf(10000),
                "PhonePe",
                "Debited from wallet",
                "Success",
                LocalDateTime.now()
        );

        Page<Payment> payments =
                paymentRepository
                        .findByUserUserId(
                                user.getUserId(),
                                PageRequest.of(0, 10)
                        );

        assertThat(
                payments.getContent()
        ).hasSize(2);
    }

    @Test
    @DisplayName(
            "Should Filter Payments By Payment Method"
    )
    void shouldFilterPaymentsByPaymentMethod() {

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

        createPayment(
                user,
                BigDecimal.valueOf(10000),
                "Google Pay",
                "Credited to wallet",
                "Success",
                LocalDateTime.now()
        );

        createPayment(
                user,
                BigDecimal.valueOf(20000),
                "PhonePe",
                "Debited from wallet",
                "Success",
                LocalDateTime.now()
        );

        Page<Payment> payments =
                paymentRepository
                        .findByUserUserIdAndPaymentMethod(
                                user.getUserId(),
                                "Google Pay",
                                PageRequest.of(0, 10)
                        );

        assertThat(
                payments.getContent()
        ).hasSize(1);

        assertThat(
                payments.getContent()
                        .get(0)
                        .getPaymentMethod()
        ).isEqualTo("Google Pay");
    }

    @Test
    @DisplayName(
            "Should Filter Payments By Transaction Type"
    )
    void shouldFilterPaymentsByTransactionType() {

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

        createPayment(
                user,
                BigDecimal.valueOf(5000),
                "Google Pay",
                "Credited to wallet",
                "Success",
                LocalDateTime.now()
        );

        createPayment(
                user,
                BigDecimal.valueOf(2000),
                "PhonePe",
                "Debited from wallet",
                "Success",
                LocalDateTime.now()
        );

        Page<Payment> payments =
                paymentRepository
                        .findByUserUserIdAndTransactionType(
                                user.getUserId(),
                                "Credited to wallet",
                                PageRequest.of(0, 10)
                        );

        assertThat(
                payments.getContent()
        ).hasSize(1);

        assertThat(
                payments.getContent()
                        .get(0)
                        .getTransactionType()
        ).isEqualTo("Credited to wallet");
    }
}