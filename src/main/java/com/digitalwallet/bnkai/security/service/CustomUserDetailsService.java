package com.digitalwallet.bnkai.security.service;

import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.repository.UserRepository;
import lombok.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final
    UserRepository
            userRepository;

    public CustomUserDetailsService(
            UserRepository
                    userRepository
    ) {

        this.userRepository =
                userRepository;
    }

    @Override
    public @NonNull UserDetails loadUserByUsername(
            @NonNull String email
    ) throws UsernameNotFoundException {

        User user =
                userRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new UsernameNotFoundException(
                                                "User not found"
                                        )
                        );

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_USER"
                        )
                )
        );
    }
}