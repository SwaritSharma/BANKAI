package com.digitalwallet.bnkai.security.service;

import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.repository.VendorRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VendorUserDetailsService
        implements UserDetailsService {

    private final
    VendorRepository
            vendorRepository;

    @Override
    public @NonNull UserDetails loadUserByUsername(
            @NonNull String email
    ) throws UsernameNotFoundException {

        Vendor vendor =
                vendorRepository
                        .findByContactEmail(
                                email
                        )
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "Vendor not found"
                                        )
                        );

        return new org.springframework.security.core.userdetails.User(
                vendor.getContactEmail(),
                vendor.getPassword(),
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_VENDOR"
                        )
                )
        );
    }
}