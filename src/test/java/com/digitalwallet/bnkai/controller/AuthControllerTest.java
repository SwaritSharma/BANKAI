package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.LoginRequest;
import com.digitalwallet.bnkai.mapper.AddressMapper;
import com.digitalwallet.bnkai.mapper.UserMapper;
import com.digitalwallet.bnkai.repository.AddressRepository;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(
        addFilters = false
)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private com.digitalwallet.bnkai.repository.VendorRepository vendorRepository;

    @MockitoBean
    private AddressRepository addressRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private AddressMapper addressMapper;

    @Test
    void shouldLoginSuccessfully()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "test@gmail.com"
        );

        request.setPassword(
                "password123"
        );

        UserDetails userDetails =
                User.builder()
                        .username(
                                "test@gmail.com"
                        )
                        .password(
                                "encodedPassword"
                        )
                        .roles(
                                "USER"
                        )
                        .build();

        when(
                authenticationManager.authenticate(
                        any()
                )
        ).thenReturn(
                (Authentication) null
        );

        when(
                userDetailsService.loadUserByUsername(
                        "test@gmail.com"
                )
        ).thenReturn(
                userDetails
        );

        com.digitalwallet.bnkai.entity.User mockUser = new com.digitalwallet.bnkai.entity.User();
        mockUser.setName("Test User");
        mockUser.setEmail("test@gmail.com");
        mockUser.setUserId(1);

        when(userRepository.findByEmail("test@gmail.com")).thenReturn(Optional.of(mockUser));

        when(
                jwtService.generateToken(
                        userDetails
                )
        ).thenReturn(
                "mocked-jwt-token"
        );

        mockMvc.perform(
                        post("/user/auth/login")
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.token")
                                .value(
                                        "mocked-jwt-token"
                                )
                );
    }
    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "test@gmail.com"
        );

        request.setPassword(
                "wrong-password"
        );

        when(
                authenticationManager.authenticate(
                        any()
                )
        ).thenThrow(
                new BadCredentialsException(
                        "Bad credentials"
                )
        );

        mockMvc.perform(
                        post("/user/auth/login")
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isUnauthorized()
                );
    }
    @Test
    void shouldReturnBadRequestForEmptyEmail()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                ""
        );

        request.setPassword(
                "password123"
        );

        mockMvc.perform(
                        post("/user/auth/login")
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void shouldReturnBadRequestForInvalidEmailFormat()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "invalid-email"
        );

        request.setPassword(
                "password123"
        );

        mockMvc.perform(
                        post("/user/auth/login")
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }

    @Test
    void shouldReturnBadRequestForEmptyPassword()
            throws Exception {

        LoginRequest request =
                new LoginRequest();

        request.setEmail(
                "test@gmail.com"
        );

        request.setPassword(
                ""
        );

        mockMvc.perform(
                        post("/user/auth/login")
                                .contentType(
                                        APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );
    }
}
