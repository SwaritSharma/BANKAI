package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.LoginRequest;
import com.digitalwallet.bnkai.dto.LoginResponse;
import com.digitalwallet.bnkai.dto.VendorRegisterRequest;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.exception.DuplicateResourceException;
import com.digitalwallet.bnkai.exception.InvalidCredentialsException;
import com.digitalwallet.bnkai.mapper.AddressMapper;
import com.digitalwallet.bnkai.mapper.VendorBranchMapper;
import com.digitalwallet.bnkai.mapper.VendorMapper;
import com.digitalwallet.bnkai.repository.AddressRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.GoldPriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/vendor/auth")
@RequiredArgsConstructor
public class VendorAuthController {

    private final
    VendorUserDetailsService
            vendorUserDetailsService;

    private final
    JwtService
            jwtService;

    private final
    PasswordEncoder
            passwordEncoder;

    private final
    VendorRepository
            vendorRepository;

    private final
    VendorBranchRepository
            vendorBranchRepository;

    private final
    AddressRepository
            addressRepository;

    private final
    GoldPriceService
            goldPriceService;

    private final VendorMapper vendorMapper;

    private final AddressMapper addressMapper;

    private final VendorBranchMapper vendorBranchMapper;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid
            @RequestBody
            LoginRequest request
    ) {

        try {

            DaoAuthenticationProvider authProvider =
                    new DaoAuthenticationProvider(
                            vendorUserDetailsService
                    );

            authProvider.setPasswordEncoder(
                    passwordEncoder
            );

            authProvider.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

        } catch (
                Exception ex
        ) {

            throw new InvalidCredentialsException(
                    "Invalid vendor credentials"
            );
        }

        UserDetails userDetails =
                vendorUserDetailsService
                        .loadUserByUsername(
                                request.getEmail()
                        );

        String token =
                jwtService.generateToken(
                        userDetails
                );

        Vendor vendor = vendorRepository.findByContactEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Vendor not found"));

        return new LoginResponse(
                token,
                vendor.getVendorName(),
                vendor.getContactEmail(),
                "VENDOR",
                vendor.getVendorId()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(
            @Valid @RequestBody VendorRegisterRequest request
    ) {
        if (vendorRepository.findByContactEmail(request.getContactEmail()).isPresent()) {
            throw new DuplicateResourceException("Email already in use by another vendor");
        }

        Vendor vendor = vendorMapper.toEntity(request);
        vendor.setPassword(passwordEncoder.encode(request.getPassword()));
        vendor.setTotalGoldQuantity(BigDecimal.ZERO);
        vendor.setCurrentGoldPrice(goldPriceService.getCurrentPrice().getPrice());
        vendor.setCreatedAt(LocalDateTime.now());

        vendor = vendorRepository.save(vendor);

        Address address = addressMapper.toEntity(request);
        address = addressRepository.save(address);

        VendorBranch branch = vendorBranchMapper.toEntity(vendor, address, BigDecimal.ZERO, LocalDateTime.now());
        vendorBranchRepository.save(branch);

        UserDetails userDetails = vendorUserDetailsService.loadUserByUsername(vendor.getContactEmail());
        String token = jwtService.generateToken(userDetails);

        LoginResponse response = new LoginResponse(
                token,
                vendor.getVendorName(),
                vendor.getContactEmail(),
                "VENDOR",
                vendor.getVendorId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
