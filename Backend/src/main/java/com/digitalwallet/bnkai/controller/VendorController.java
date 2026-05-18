package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.VendorDTO;
import com.digitalwallet.bnkai.mapper.VendorMapper;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.service.GoldPriceService;
import lombok.RequiredArgsConstructor;
import com.digitalwallet.bnkai.dto.AddBranchRequest;
import com.digitalwallet.bnkai.dto.VendorBranchDTO;
import com.digitalwallet.bnkai.dto.VendorDashboardDTO;
import com.digitalwallet.bnkai.dto.EditVendorProfileRequest;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.dto.AddGoldRequest;
import com.digitalwallet.bnkai.service.VendorDashboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.VENDORS_CACHE;

@RestController
@RequestMapping("/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository vendorRepository;
    private final GoldPriceService goldPriceService;
    private final VendorMapper vendorMapper;

    @GetMapping
    @Cacheable(cacheNames = VENDORS_CACHE, key = "'all'")
    public List<VendorDTO> getVendors() {
        return vendorRepository.findAll().stream()
                .map(vendor -> vendorMapper.toDto(
                        vendor,
                        vendor.getCurrentGoldPrice() != null
                                ? vendor.getCurrentGoldPrice()
                                : goldPriceService.getCurrentPrice().getPrice()
                ))
                .toList();
    }

    private final VendorDashboardService vendorDashboardService;

    @GetMapping("/{id}/dashboard")
    public ResponseEntity<VendorDashboardDTO> getDashboard(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(vendorDashboardService.getDashboard(id));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<VendorDashboardDTO> updateProfile(@PathVariable("id") Integer id, @Valid @RequestBody EditVendorProfileRequest request) {
        return ResponseEntity.ok(vendorDashboardService.updateProfile(id, request));
    }

    @GetMapping("/{id}/branches")
    public ResponseEntity<List<VendorBranchDTO>> getBranches(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(vendorDashboardService.getBranches(id));
    }

    @PostMapping("/{id}/branches")
    public ResponseEntity<VendorBranchDTO> addBranch(@PathVariable("id") Integer id, @Valid @RequestBody AddBranchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorDashboardService.addBranch(id, request));
    }

    @DeleteMapping("/{id}/branches/{branchId}")
    public ResponseEntity<Void> deleteBranch(@PathVariable("id") Integer id, @PathVariable("branchId") Integer branchId) {
        vendorDashboardService.deleteBranch(id, branchId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<List<TransactionDTO>> getTransactions(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(vendorDashboardService.getTransactions(id));
    }

    @PostMapping("/{id}/add-gold")
    public ResponseEntity<Void> addGold(@PathVariable("id") Integer id, @Valid @RequestBody AddGoldRequest request) {
        vendorDashboardService.addGoldToBranch(id, request.getBranchId(), request.getQuantity());
        return ResponseEntity.ok().build();
    }
}
