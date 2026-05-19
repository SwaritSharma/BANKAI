package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.*;

import java.util.List;

public interface DashboardService {

    DashboardDTO getDashboard(Integer userId);

    void updateProfile(Integer userId, EditProfileRequest request);

    List<HoldingDTO> getHoldings(Integer userId);

    List<TransactionDTO> getTransactions(Integer userId);

    List<HoldingDTO.AddressDTO> getAddresses(Integer userId);

    List<PhysicalGoldDTO> getPhysicalGold(Integer userId);

    List<PaymentDTO> getPayments(Integer userId);
}
