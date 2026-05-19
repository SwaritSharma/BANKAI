package com.digitalwallet.bnkai.security.filter;

import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private VendorUserDetailsService vendorUserDetailsService;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_missingAuthorizationHeader_continuesWithoutAuthentication() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, vendorUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wallet/topup");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).extractUsername(org.mockito.ArgumentMatchers.anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validUserToken_setsSecurityContextAuthentication() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, vendorUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wallet/topup");
        request.addHeader("Authorization", "Bearer valid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        UserDetails userDetails = User.withUsername("pradeep.kumar@example.in").password("encoded").roles("USER").build();

        when(jwtService.extractUsername("valid-token")).thenReturn("pradeep.kumar@example.in");
        when(customUserDetailsService.loadUserByUsername("pradeep.kumar@example.in")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("pradeep.kumar@example.in");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_userLookupFailsAndVendorTokenValid_setsVendorAuthentication() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, vendorUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/vendors/1/dashboard");
        request.addHeader("Authorization", "Bearer vendor-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        UserDetails vendorDetails = User.withUsername("vendor@example.in").password("encoded").roles("VENDOR").build();

        when(jwtService.extractUsername("vendor-token")).thenReturn("vendor@example.in");
        when(customUserDetailsService.loadUserByUsername("vendor@example.in"))
                .thenThrow(new UsernameNotFoundException("User not found"));
        when(vendorUserDetailsService.loadUserByUsername("vendor@example.in")).thenReturn(vendorDetails);
        when(jwtService.isTokenValid("vendor-token", vendorDetails)).thenReturn(true);

        filter.doFilter(request, response, filterChain);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("vendor@example.in");
        assertThat(authentication.getAuthorities()).extracting("authority").containsExactly("ROLE_VENDOR");
    }

    @Test
    void doFilterInternal_invalidToken_doesNotAuthenticate() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, vendorUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wallet/topup");
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        UserDetails userDetails = User.withUsername("pradeep.kumar@example.in").password("encoded").roles("USER").build();

        when(jwtService.extractUsername("invalid-token")).thenReturn("pradeep.kumar@example.in");
        when(customUserDetailsService.loadUserByUsername("pradeep.kumar@example.in")).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid-token", userDetails)).thenReturn(false);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_jwtParsingFails_returnsStructuredUnauthorizedResponse() throws Exception {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, customUserDetailsService, vendorUserDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/wallet/topup");
        request.addHeader("Authorization", "Bearer malformed-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractUsername("malformed-token")).thenThrow(new JwtException("Malformed JWT"));

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).contains("Invalid or expired JWT token");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain, never()).doFilter(request, response);
    }
}
