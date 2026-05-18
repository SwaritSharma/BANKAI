package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.LoginRequest;
import com.digitalwallet.bnkai.dto.LoginResponse;
import com.digitalwallet.bnkai.dto.RegisterRequest;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.exception.DuplicateResourceException;
import com.digitalwallet.bnkai.exception.InvalidCredentialsException;
import com.digitalwallet.bnkai.mapper.AddressMapper;
import com.digitalwallet.bnkai.mapper.UserMapper;
import com.digitalwallet.bnkai.repository.AddressRepository;
import com.digitalwallet.bnkai.repository.UserRepository;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class AuthController {

    private final
    AuthenticationManager
            authenticationManager;

    private final
    CustomUserDetailsService
            userDetailsService;

    private final
    JwtService
            jwtService;

    private final
    UserRepository
            userRepository;

    private final
    AddressRepository
            addressRepository;

    private final
    PasswordEncoder
            passwordEncoder;

    private final UserMapper userMapper;

    private final AddressMapper addressMapper;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (
                BadCredentialsException ex
        ) {

            throw new InvalidCredentialsException(
                    "Invalid email or password"
            );
        }

        UserDetails userDetails =
                userDetailsService
                        .loadUserByUsername(
                                request.getEmail()
                        );

        String token =
                jwtService.generateToken(
                        userDetails
                );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return new LoginResponse(
                token,
                user.getName(),
                user.getEmail(),
                "USER",
                user.getUserId()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use");
        }

        Address address = addressMapper.toEntity(request);
        address = addressRepository.save(address);

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBalance(BigDecimal.ZERO);
        user.setCreatedAt(LocalDateTime.now());
        user.setAddress(address);
        
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse(
                token,
                user.getName(),
                user.getEmail(),
                "USER",
                user.getUserId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
