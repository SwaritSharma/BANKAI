package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.AddBranchRequest;
import com.digitalwallet.bnkai.dto.EditVendorProfileRequest;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.dto.VendorBranchDTO;
import com.digitalwallet.bnkai.dto.VendorDashboardDTO;

import java.math.BigDecimal;
import java.util.List;

public interface VendorDashboardService {

    VendorDashboardDTO getDashboard(Integer vendorId);

    VendorDashboardDTO updateProfile(Integer vendorId, EditVendorProfileRequest request);

    List<VendorBranchDTO> getBranches(Integer vendorId);

    VendorBranchDTO addBranch(Integer vendorId, AddBranchRequest request);

    void deleteBranch(Integer vendorId, Integer branchId);

    List<TransactionDTO> getTransactions(Integer vendorId);

    void addGoldToBranch(Integer vendorId, Integer branchId, BigDecimal quantity);
}
