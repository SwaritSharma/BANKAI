package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.exception.InsufficientHoldingQuantityException;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.VirtualGoldService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
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

    @Test
    void buyVirtualGold_validRequest_returnsHolding() throws Exception {
        BuyVirtualGoldRequest request = new BuyVirtualGoldRequest(1, 2, new BigDecimal("1.25"));
        HoldingDTO dto = new HoldingDTO();
        dto.setHoldingId(10);
        dto.setQuantity(new BigDecimal("1.25"));

        when(virtualGoldService.buyVirtualGold(any(BuyVirtualGoldRequest.class))).thenReturn(dto);

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
        BuyVirtualGoldRequest request = new BuyVirtualGoldRequest(1, 2, new BigDecimal("0.00000000"));

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
        HoldingDTO dto = new HoldingDTO();
        dto.setHoldingId(10);
        dto.setQuantity(new BigDecimal("0.75"));

        when(virtualGoldService.sellVirtualGold(any(SellVirtualGoldRequest.class))).thenReturn(dto);

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
