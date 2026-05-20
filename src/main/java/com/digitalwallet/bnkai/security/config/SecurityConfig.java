package com.digitalwallet.bnkai.security.config;

import com.digitalwallet.bnkai.security.filter.JwtAuthenticationFilter;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final
    CustomUserDetailsService
            customUserDetailsService;

    private final
    VendorUserDetailsService
            vendorUserDetailsService;

    private final
    JwtAuthenticationFilter
            jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origin-patterns:http://localhost:3000}")
    private String allowedOriginPatterns;

    @Bean
    public SecurityFilterChain
    securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .csrf(
                        csrf ->
                                csrf.disable()
                )
                .cors(
                        Customizer.withDefaults()
                )
                .authorizeHttpRequests(
                        auth ->
                                auth

                                        .requestMatchers(
                                                "/user/auth/**",
                                                "/vendor/auth/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html",
                                                "/swagger-ui/**"
                                        )
                                        .permitAll()

                                        .requestMatchers(
                                                "/users/**",
                                                "/wallet/**",
                                                "/virtual-gold/**",
                                                "/physical-gold/**"
                                        )
                                        .hasRole(
                                                "USER"
                                        )

                                        .requestMatchers(
                                                "/vendors"
                                        )
                                        .hasAnyRole(
                                                "USER",
                                                "VENDOR"
                                        )

                                        .requestMatchers(
                                                "/vendors/**"
                                        )
                                        .hasRole(
                                                "VENDOR"
                                        )

                                        .anyRequest()
                                        .authenticated()
                )
                .sessionManagement(
                        session ->
                                session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS
                                )
                )
                .exceptionHandling(exceptions ->
                        exceptions
                                .authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler())
                )
                .authenticationProvider(
                        authenticationProvider()
                )
                .authenticationProvider(
                        vendorAuthenticationProvider()
                )
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder
    passwordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager
    authenticationManager() {
        return new ProviderManager(
                authenticationProvider(),
                vendorAuthenticationProvider()
        );
    }

    @Bean
    public DaoAuthenticationProvider
    authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(
                        customUserDetailsService
                );

        authProvider.setPasswordEncoder(
                passwordEncoder()
        );

        return authProvider;
    }

    @Bean
    public DaoAuthenticationProvider
    vendorAuthenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(
                        vendorUserDetailsService
                );

        authProvider.setPasswordEncoder(
                passwordEncoder()
        );

        return authProvider;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeSecurityError(
                response,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                request.getRequestURI()
        );
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeSecurityError(
                response,
                HttpStatus.FORBIDDEN,
                "Access denied",
                request.getRequestURI()
        );
    }

    private void writeSecurityError(
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String message,
            String path
    ) throws IOException {

        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\","
                        + "\"status\":" + status.value() + ","
                        + "\"error\":\"" + status.getReasonPhrase() + "\","
                        + "\"message\":\"" + message + "\","
                        + "\"details\":[\"" + message + "\"],"
                        + "\"path\":\"" + path + "\"}"
        );
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.stream(allowedOriginPatterns.split(","))
                .map(String::trim)
                .filter(pattern -> !pattern.isEmpty())
                .toList());
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("X-New-Token"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
