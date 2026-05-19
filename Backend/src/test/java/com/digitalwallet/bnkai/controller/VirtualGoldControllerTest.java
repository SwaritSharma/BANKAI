package com.digitalwallet.bnkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.entity.VirtualGoldHolding;
import com.digitalwallet.bnkai.exception.InsufficientHoldingQuantityException;
import com.digitalwallet.bnkai.mapper.HoldingMapper;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.VirtualGoldService;
import com.digitalwallet.bnkai.service.GoldPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VirtualGoldController.class)
@AutoConfigureMockMvc(addFilters = false)
class VirtualGoldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VirtualGoldService virtualGoldService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @MockitoBean
    private HoldingMapper holdingMapper;

    @MockitoBean
    private GoldPriceService goldPriceService;

    @Test
    void buyVirtualGold_validRequest_returnsHolding() throws Exception {
        BuyVirtualGoldRequest request = new BuyVirtualGoldRequest(1, 2, new BigDecimal("1.25"));
        VirtualGoldHolding holding = new VirtualGoldHolding();
        holding.setHoldingId(10);
        holding.setQuantity(new BigDecimal("1.25"));
        HoldingDTO dto = new HoldingDTO();
        dto.setHoldingId(10);
        dto.setQuantity(new BigDecimal("1.25"));
        GoldPriceDTO price = new GoldPriceDTO();
        price.setPrice(new BigDecimal("7150.00"));

        when(virtualGoldService.buyVirtualGold(any(BuyVirtualGoldRequest.class))).thenReturn(holding);
        when(goldPriceService.getCurrentPrice()).thenReturn(price);
        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class))).thenReturn(dto);

        mockMvc.perform(post("/virtual-gold/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holding_id").value(10))
                .andExpect(jsonPath("$.quantity").value(1.25));

        verify(virtualGoldService).buyVirtualGold(any(BuyVirtualGoldRequest.class));
    }

    @Test
    void buyVirtualGold_quantityBelowMinimum_returnsBadRequest() throws Exception {
        BuyVirtualGoldRequest request = new BuyVirtualGoldRequest(1, 2, new BigDecimal("0.01"));

        mockMvc.perform(post("/virtual-gold/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Quantity must be greater than 0"));

        verify(virtualGoldService, never()).buyVirtualGold(any(BuyVirtualGoldRequest.class));
    }

    @Test
    void sellVirtualGold_validRequest_returnsUpdatedHolding() throws Exception {
        SellVirtualGoldRequest request = new SellVirtualGoldRequest(1, 10, new BigDecimal("0.50"));
        VirtualGoldHolding holding = new VirtualGoldHolding();
        holding.setHoldingId(10);
        holding.setQuantity(new BigDecimal("0.75"));
        HoldingDTO dto = new HoldingDTO();
        dto.setHoldingId(10);
        dto.setQuantity(new BigDecimal("0.75"));
        GoldPriceDTO price = new GoldPriceDTO();
        price.setPrice(new BigDecimal("7150.00"));

        when(virtualGoldService.sellVirtualGold(any(SellVirtualGoldRequest.class))).thenReturn(holding);
        when(goldPriceService.getCurrentPrice()).thenReturn(price);
        when(holdingMapper.toDto(any(VirtualGoldHolding.class), any(BigDecimal.class))).thenReturn(dto);

        mockMvc.perform(post("/virtual-gold/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holding_id").value(10))
                .andExpect(jsonPath("$.quantity").value(0.75));

        verify(virtualGoldService).sellVirtualGold(any(SellVirtualGoldRequest.class));
    }

    @Test
    void sellVirtualGold_insufficientHolding_returnsBadRequest() throws Exception {
        SellVirtualGoldRequest request = new SellVirtualGoldRequest(1, 10, new BigDecimal("5.00"));
        when(virtualGoldService.sellVirtualGold(any(SellVirtualGoldRequest.class)))
                .thenThrow(new InsufficientHoldingQuantityException("Insufficient holding quantity"));

        mockMvc.perform(post("/virtual-gold/sell")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient holding quantity"));
    }
}
