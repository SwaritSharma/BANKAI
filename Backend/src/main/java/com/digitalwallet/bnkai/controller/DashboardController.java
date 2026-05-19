package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.DashboardDTO;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.dto.EditProfileRequest;
import com.digitalwallet.bnkai.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @GetMapping("/{id}/dashboard")
    public DashboardDTO getDashboard(@PathVariable("id") Integer id) {
        return dashboardService.getDashboard(id);
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<Void> updateProfile(@PathVariable("id") Integer id, @Valid @RequestBody EditProfileRequest request) {
        dashboardService.updateProfile(id, request);
        String token = jwtService.generateToken(userDetailsService.loadUserByUsername(request.getEmail()));
        return ResponseEntity.ok()
                .header("X-New-Token", token)
                .build();
    }

    @GetMapping("/{id}/holdings")
    public List<HoldingDTO> getHoldings(@PathVariable("id") Integer id) {
        return dashboardService.getHoldings(id);
    }

    @GetMapping("/{id}/transactions")
    public List<TransactionDTO> getTransactions(@PathVariable("id") Integer id) {
        return dashboardService.getTransactions(id);
    }

    @GetMapping("/{id}/addresses")
    public List<HoldingDTO.AddressDTO> getAddresses(@PathVariable("id") Integer id) {
        return dashboardService.getAddresses(id);
    }

    @GetMapping("/{id}/physical-gold")
    public List<PhysicalGoldDTO> getPhysicalGold(@PathVariable("id") Integer id) {
        return dashboardService.getPhysicalGold(id);
    }

    @GetMapping("/{id}/payments")
    public List<com.digitalwallet.bnkai.dto.PaymentDTO> getPayments(@PathVariable("id") Integer id) {
        return dashboardService.getPayments(id);
    }
}
