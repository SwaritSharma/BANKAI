package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.DashboardDTO;
import com.digitalwallet.bnkai.dto.EditProfileRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.PaymentDTO;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.dto.TransactionDTO;

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
