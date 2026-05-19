package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.VendorDashboardDTO;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.mapper.VendorDashboardMapper;
import com.digitalwallet.bnkai.repository.TransactionHistoryRepository;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.service.impl.VendorDashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VendorDashboardServiceImplTest {

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private VendorBranchRepository vendorBranchRepository;

    @Mock
    private TransactionHistoryRepository transactionRepository;

    @Mock
    private GoldPriceService goldPriceService;

    @Mock
    private VendorDashboardMapper vendorDashboardMapper;

    @InjectMocks
    private VendorDashboardServiceImpl vendorDashboardService;

    private Vendor vendor;
    private VendorBranch branch1;
    private VendorBranch branch2;

    @BeforeEach
    void setUp() {
        vendor = new Vendor();
        vendor.setVendorId(1);
        vendor.setVendorName("Bullion Corp");

        branch1 = new VendorBranch();
        branch1.setBranchId(10);
        branch1.setQuantity(new BigDecimal("150.50"));

        branch2 = new VendorBranch();
        branch2.setBranchId(11);
        branch2.setQuantity(new BigDecimal("200.25"));
    }

    @Test
    void getDashboard_ShouldCalculateMetricsCorrectly() {
        // Arrange
        when(vendorRepository.findById(1)).thenReturn(Optional.of(vendor));
        when(vendorBranchRepository.findByVendorVendorId(1)).thenReturn(Arrays.asList(branch1, branch2));
        
        BigDecimal expectedSoldQuantity = new BigDecimal("450.75");
        when(transactionRepository.sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(1))
                .thenReturn(expectedSoldQuantity);

        GoldPriceDTO goldPrice = new GoldPriceDTO();
        goldPrice.setPrice(new BigDecimal("7250.00"));
        when(goldPriceService.getCurrentPrice()).thenReturn(goldPrice);

        VendorDashboardDTO expectedDto = new VendorDashboardDTO();
        expectedDto.setVendorId(1);
        expectedDto.setVendorName("Bullion Corp");
        expectedDto.setTotalBranches(2);
        expectedDto.setTotalGoldQuantity(new BigDecimal("350.75")); // branch1 + branch2
        expectedDto.setTotalSoldQuantity(expectedSoldQuantity);
        expectedDto.setCurrentGoldPrice(new BigDecimal("7250.00"));

        when(vendorDashboardMapper.toDashboard(
                eq(vendor),
                eq(2),
                eq(new BigDecimal("350.75")),
                eq(expectedSoldQuantity),
                eq(new BigDecimal("7250.00"))
        )).thenReturn(expectedDto);

        // Act
        VendorDashboardDTO dashboard = vendorDashboardService.getDashboard(1);

        // Assert
        assertEquals(1, dashboard.getVendorId());
        assertEquals("Bullion Corp", dashboard.getVendorName());
        assertEquals(2, dashboard.getTotalBranches());
        assertEquals(new BigDecimal("350.75"), dashboard.getTotalGoldQuantity());
        assertEquals(expectedSoldQuantity, dashboard.getTotalSoldQuantity());
        assertEquals(new BigDecimal("7250.00"), dashboard.getCurrentGoldPrice());

        verify(vendorRepository).findById(1);
        verify(vendorBranchRepository).findByVendorVendorId(1);
        verify(transactionRepository).sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(1);
        verify(goldPriceService).getCurrentPrice();
        verify(vendorDashboardMapper).toDashboard(any(), any(), any(), any(), any());
    }

    @Test
    void getDashboard_ShouldHandleEmptyBranchesAndNullQuantities() {
        // Arrange
        when(vendorRepository.findById(1)).thenReturn(Optional.of(vendor));
        when(vendorBranchRepository.findByVendorVendorId(1)).thenReturn(Collections.emptyList());
        
        when(transactionRepository.sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(1))
                .thenReturn(BigDecimal.ZERO);

        GoldPriceDTO goldPrice = new GoldPriceDTO();
        goldPrice.setPrice(new BigDecimal("7250.00"));
        when(goldPriceService.getCurrentPrice()).thenReturn(goldPrice);

        VendorDashboardDTO expectedDto = new VendorDashboardDTO();
        expectedDto.setVendorId(1);
        expectedDto.setVendorName("Bullion Corp");
        expectedDto.setTotalBranches(0);
        expectedDto.setTotalGoldQuantity(BigDecimal.ZERO);
        expectedDto.setTotalSoldQuantity(BigDecimal.ZERO);
        expectedDto.setCurrentGoldPrice(new BigDecimal("7250.00"));

        when(vendorDashboardMapper.toDashboard(
                eq(vendor),
                eq(0),
                eq(BigDecimal.ZERO),
                eq(BigDecimal.ZERO),
                eq(new BigDecimal("7250.00"))
        )).thenReturn(expectedDto);

        // Act
        VendorDashboardDTO dashboard = vendorDashboardService.getDashboard(1);

        // Assert
        assertEquals(0, dashboard.getTotalBranches());
        assertEquals(BigDecimal.ZERO, dashboard.getTotalGoldQuantity());
        assertEquals(BigDecimal.ZERO, dashboard.getTotalSoldQuantity());

        verify(vendorRepository).findById(1);
        verify(vendorBranchRepository).findByVendorVendorId(1);
        verify(transactionRepository).sumQuantityByVendorIdAndTransactionTypeAndTransactionStatus(1);
        verify(goldPriceService).getCurrentPrice();
    }
}
