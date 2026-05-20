package com.digitalwallet.bnkai.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class JwtServiceTest {

    private static final String SECRET_KEY = "mysecretkeymysecretkeymysecretkey12";

    private final JwtService jwtService = new JwtService();

    @Test
    void generateToken_validUserDetails_containsUsernameAndIsValid() {
        UserDetails userDetails = User.withUsername("pradeep.kumar@example.in")
                .password("encoded")
                .roles("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        assertThat(jwtService.extractUsername(token)).isEqualTo("pradeep.kumar@example.in");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_differentUsername_returnsFalse() {
        UserDetails tokenOwner = User.withUsername("owner@example.in").password("encoded").roles("USER").build();
        UserDetails otherUser = User.withUsername("other@example.in").password("encoded").roles("USER").build();

        String token = jwtService.generateToken(tokenOwner);

        assertThat(jwtService.isTokenValid(token, otherUser)).isFalse();
    }

    @Test
    void extractUsername_expiredToken_throwsExpiredJwtException() {
        String token = Jwts.builder()
                .subject("expired@example.in")
                .issuedAt(new Date(System.currentTimeMillis() - 10_000))
                .expiration(new Date(System.currentTimeMillis() - 1_000))
                .signWith(signingKey())
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void extractUsername_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtService.extractUsername("not-a-jwt"))
                .isInstanceOf(RuntimeException.class);
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
