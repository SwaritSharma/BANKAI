package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.dto.*;
import com.digitalwallet.bnkai.entity.Address;
import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.exception.BranchAllocationException;
import com.digitalwallet.bnkai.exception.DuplicateResourceException;
import com.digitalwallet.bnkai.exception.InvalidQuantityException;
import com.digitalwallet.bnkai.exception.VendorNotFoundException;
import com.digitalwallet.bnkai.mapper.*;
import com.digitalwallet.bnkai.repository.*;
import com.digitalwallet.bnkai.service.GoldPriceService;
import com.digitalwallet.bnkai.service.VendorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.digitalwallet.bnkai.config.RedisCacheConfig.*;

@Service
@RequiredArgsConstructor
public class VendorDashboardServiceImpl implements VendorDashboardService {

    private final VendorRepository vendorRepository;
    private final UserRepository userRepository;
    private final VendorBranchRepository vendorBranchRepository;
    private final TransactionHistoryRepository transactionRepository;
    private final AddressRepository addressRepository;
    private final GoldPriceService goldPriceService;
    private final VendorMapper vendorMapper;
    private final AddressMapper addressMapper;
    private final VendorBranchMapper vendorBranchMapper;
    private final TransactionMapper transactionMapper;
    private final VendorDashboardMapper vendorDashboardMapper;
    private final VirtualGoldHoldingRepository holdingRepository;

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = VENDOR_DASHBOARD_CACHE, key = "#vendorId")
    public VendorDashboardDTO getDashboard(Integer vendorId) {
        Vendor vendor = validateAndGetVendor(vendorId);
        
        List<VendorBranch> branches = vendorBranchRepository.findByVendorVendorId(vendorId);
        BigDecimal totalInventory = branches.stream()
                .map(branch -> branch.getQuantity() != null ? branch.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSold = transactionRepository.sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(vendorId); 
        
        return vendorDashboardMapper.toDashboard(
                vendor,
                branches.size(),
                totalInventory,
                totalSold,
                goldPriceService.getCurrentPrice().getPrice()
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {
            VENDOR_DASHBOARD_CACHE,
            VENDORS_CACHE
    }, key = "#vendorId")
    public VendorDashboardDTO updateProfile(Integer vendorId, EditVendorProfileRequest request) {
        Vendor vendor = validateAndGetVendor(vendorId);
        if (request.getContactEmail() != null && !request.getContactEmail().equalsIgnoreCase(vendor.getContactEmail())) {
            if (vendorRepository.findByContactEmail(request.getContactEmail()).isPresent() || userRepository.findByEmail(request.getContactEmail()).isPresent()) {
                throw new DuplicateResourceException("Email already in use");
            }
        }
        vendorMapper.updateProfile(request, vendor);
        vendor = vendorRepository.save(vendor);

        List<VendorBranch> branches = vendorBranchRepository.findByVendorVendorId(vendorId);
        BigDecimal totalInventory = branches.stream()
                .map(branch -> branch.getQuantity() != null ? branch.getQuantity() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSold = transactionRepository.sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(vendorId); 
        
        return vendorDashboardMapper.toDashboard(
                vendor,
                branches.size(),
                totalInventory,
                totalSold,
                goldPriceService.getCurrentPrice().getPrice()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = VENDOR_BRANCHES_CACHE, key = "#vendorId")
    public List<VendorBranchDTO> getBranches(Integer vendorId) {
        validateAndGetVendor(vendorId);
        return vendorBranchMapper.toDtoList(vendorBranchRepository.findByVendorVendorId(vendorId));
    }

    @Transactional
    @CacheEvict(cacheNames = {
            VENDOR_DASHBOARD_CACHE,
            VENDOR_BRANCHES_CACHE,
            VENDOR_TRANSACTIONS_CACHE,
            VENDORS_CACHE
    }, key = "#vendorId")
    public VendorBranchDTO addBranch(Integer vendorId, AddBranchRequest request) {
        Vendor vendor = validateAndGetVendor(vendorId);
        
        Address address = addressMapper.toEntity(request);
        address = addressRepository.save(address);

        VendorBranch branch = vendorBranchMapper.toEntity(vendor, address, request.getInitialQuantity(), LocalDateTime.now());
        branch = vendorBranchRepository.save(branch);

        if (request.getInitialQuantity() != null && request.getInitialQuantity().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentVendorQty = vendor.getTotalGoldQuantity() != null ? vendor.getTotalGoldQuantity() : BigDecimal.ZERO;
            vendor.setTotalGoldQuantity(currentVendorQty.add(request.getInitialQuantity()));
            vendorRepository.save(vendor);

            TransactionHistory th = transactionMapper.toEntity(
                    null,
                    branch,
                    request.getInitialQuantity(),
                    BigDecimal.ZERO,
                    "Add Inventory",
                    "Success",
                    LocalDateTime.now()
            );
            transactionRepository.save(th);
        }

        return vendorBranchMapper.toDto(branch);
    }

    @Transactional
    @CacheEvict(cacheNames = {
            VENDOR_DASHBOARD_CACHE,
            VENDOR_BRANCHES_CACHE
    }, key = "#vendorId")
    public void deleteBranch(Integer vendorId, Integer branchId) {
        validateAndGetVendor(vendorId);
        VendorBranch branch = vendorBranchRepository.findById(branchId).orElseThrow(() -> new BranchAllocationException("Branch not found"));
        if (branch.getVendor() == null || !branch.getVendor().getVendorId().equals(vendorId)) {
            throw new BranchAllocationException("Branch does not belong to this vendor");
        }
        if (transactionRepository.existsByBranchBranchId(branchId) || holdingRepository.existsByBranchBranchId(branchId)) {
            throw new BranchAllocationException("Cannot delete branch because it has active holdings or transaction history");
        }
        vendorBranchRepository.delete(branch);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = VENDOR_TRANSACTIONS_CACHE, key = "#vendorId")
    public List<TransactionDTO> getTransactions(Integer vendorId) {
        validateAndGetVendor(vendorId);
        return transactionMapper.toDtoList(
                transactionRepository.findByBranchVendorVendorIdOrderByCreatedAtDesc(vendorId, PageRequest.of(0, 100)).getContent()
        );
    }

    @Transactional
    @CacheEvict(cacheNames = {
            VENDOR_DASHBOARD_CACHE,
            VENDOR_BRANCHES_CACHE,
            VENDOR_TRANSACTIONS_CACHE,
            VENDORS_CACHE
    }, key = "#vendorId")
    public void addGoldToBranch(Integer vendorId, Integer branchId, BigDecimal quantity) {
        validateAndGetVendor(vendorId);
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidQuantityException("Quantity must be positive");
        }

        VendorBranch branch = vendorBranchRepository.findById(branchId).orElseThrow(() -> new BranchAllocationException("Branch not found"));
        if (branch.getVendor() == null || !branch.getVendor().getVendorId().equals(vendorId)) {
            throw new BranchAllocationException("Branch does not belong to this vendor");
        }
        
        BigDecimal currentBranchQty = branch.getQuantity() != null ? branch.getQuantity() : BigDecimal.ZERO;
        branch.setQuantity(currentBranchQty.add(quantity));
        vendorBranchRepository.save(branch);
        
        Vendor vendor = branch.getVendor();
        BigDecimal currentVendorQty = vendor.getTotalGoldQuantity() != null ? vendor.getTotalGoldQuantity() : BigDecimal.ZERO;
        vendor.setTotalGoldQuantity(currentVendorQty.add(quantity));
        vendorRepository.save(vendor);
        
        TransactionHistory th = transactionMapper.toEntity(
                null,
                branch,
                quantity,
                BigDecimal.ZERO,
                "Add Inventory",
                "Success",
                LocalDateTime.now()
        );
        transactionRepository.save(th);
    }

    private Vendor validateAndGetVendor(Integer vendorId) {
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new VendorNotFoundException("Vendor not found"));
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !vendor.getContactEmail().equalsIgnoreCase(auth.getName())) {
            throw new AccessDeniedException("Access denied");
        }
        return vendor;
    }
}
