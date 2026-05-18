package com.digitalwallet.bnkai.repository;

import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
class UserRepositoryTest {

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

    @Test
    @DisplayName(
            "Should Return True When Email Exists"
    )
    void shouldReturnTrueWhenEmailExists() {

        Address address =
                createAddress(
                        "Chandigarh",
                        "160017"
                );

        createUser(
                "Friday",
                "friday@gmail.com",
                BigDecimal.valueOf(50000),
                address
        );

        boolean exists =
                userRepository.existsByEmail(
                        "friday@gmail.com"
                );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName(
            "Should Return False When Email Does Not Exist"
    )
    void shouldReturnFalseWhenEmailDoesNotExist() {

        boolean exists =
                userRepository.existsByEmail(
                        "unknown@gmail.com"
                );

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName(
            "Should Validate Unique Email Persistence"
    )
    void shouldValidateUniqueEmailPersistence() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        createUser(
                "Aman",
                "aman@gmail.com",
                BigDecimal.valueOf(30000),
                address
        );

        boolean exists =
                userRepository.existsByEmail(
                        "aman@gmail.com"
                );

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName(
            "Should Work With Multiple Users"
    )
    void shouldWorkWithMultipleUsers() {

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

        createUser(
                "User1",
                "user1@gmail.com",
                BigDecimal.valueOf(20000),
                address1
        );

        createUser(
                "User2",
                "user2@gmail.com",
                BigDecimal.valueOf(40000),
                address2
        );

        assertThat(
                userRepository.existsByEmail(
                        "user1@gmail.com"
                )
        ).isTrue();

        assertThat(
                userRepository.existsByEmail(
                        "user2@gmail.com"
                )
        ).isTrue();
    }

    @Test
    @DisplayName(
            "Should Find User By Email"
    )
    void shouldFindUserByEmail() {

        Address address =
                createAddress(
                        "Chandigarh",
                        "160017"
                );

        createUser(
                "Friday",
                "friday@gmail.com",
                BigDecimal.valueOf(50000),
                address
        );

        Optional<User> foundUser =
                userRepository.findByEmail(
                        "friday@gmail.com"
                );

        assertThat(foundUser).isPresent();

        assertThat(
                foundUser.get().getName()
        ).isEqualTo("Friday");
    }

    @Test
    @DisplayName(
            "Should Return Empty When Email Not Found"
    )
    void shouldReturnEmptyWhenEmailNotFound() {

        Optional<User> foundUser =
                userRepository.findByEmail(
                        "invalid@gmail.com"
                );

        assertThat(foundUser).isEmpty();
    }

    @Test
    @DisplayName(
            "Should Fetch Correct User Balance"
    )
    void shouldFetchCorrectUserBalance() {

        Address address =
                createAddress(
                        "Delhi",
                        "110001"
                );

        createUser(
                "Aman",
                "aman@gmail.com",
                BigDecimal.valueOf(75000),
                address
        );

        Optional<User> foundUser =
                userRepository.findByEmail(
                        "aman@gmail.com"
                );

        assertThat(foundUser).isPresent();

        assertThat(
                foundUser.get().getBalance()
        ).isEqualTo(
                BigDecimal.valueOf(75000)
        );
    }

    @Test
    @DisplayName(
            "Should Fetch Linked Address Correctly"
    )
    void shouldFetchLinkedAddressCorrectly() {

        Address address =
                createAddress(
                        "Mumbai",
                        "400001"
                );

        createUser(
                "Rohit",
                "rohit@gmail.com",
                BigDecimal.valueOf(60000),
                address
        );

        Optional<User> foundUser =
                userRepository.findByEmail(
                        "rohit@gmail.com"
                );

        assertThat(foundUser).isPresent();

        assertThat(
                foundUser.get()
                        .getAddress()
                        .getCity()
        ).isEqualTo("Mumbai");
    }
}