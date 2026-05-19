package com.digitalwallet.bnkai.security.filter;

import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final
    JwtService
            jwtService;

    private final
    CustomUserDetailsService
            userDetailsService;

    private final
    VendorUserDetailsService
            vendorUserDetailsService;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request
    ) {

        String path =
                request.getServletPath();

        return path.startsWith(
                "/user/auth"
        ) || path.startsWith(
                "/vendor/auth"
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader =
                request.getHeader(
                        "Authorization"
                );

        final String jwt;

        final String email;

        if (
                authHeader == null
                        || !authHeader.startsWith(
                        "Bearer "
                )
        ) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        jwt =
                authHeader.substring(7);

        try {
            email =
                    jwtService.extractUsername(jwt);
        } catch (JwtException | IllegalArgumentException ex) {
            SecurityContextHolder.clearContext();
            writeUnauthorizedResponse(request, response, "Invalid or expired JWT token");
            return;
        }

        if (
                email != null
                        &&
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                == null
        ) {

            UserDetails userDetails;

            try {

                userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        email
                                );

            } catch (
                    UsernameNotFoundException ex
            ) {

                userDetails =
                        vendorUserDetailsService
                                .loadUserByUsername(
                                        email
                                );
            }

            boolean tokenValid;

            try {
                tokenValid = jwtService.isTokenValid(
                        jwt,
                        userDetails
                );
            } catch (JwtException | IllegalArgumentException ex) {
                SecurityContextHolder.clearContext();
                writeUnauthorizedResponse(request, response, "Invalid or expired JWT token");
                return;
            }

            if (tokenValid) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authToken
                        );
            }
        }

        filterChain.doFilter(
                request,
                response
        );
    }


    private void writeUnauthorizedResponse(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"timestamp\":\"" + LocalDateTime.now() + "\","
                        + "\"status\":401,"
                        + "\"error\":\"Unauthorized\","
                        + "\"message\":\"" + message + "\","
                        + "\"details\":[\"" + message + "\"],"
                        + "\"path\":\"" + request.getRequestURI() + "\"}"
        );
    }
}
