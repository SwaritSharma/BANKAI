package com.digitalwallet.bnkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.entity.PhysicalGoldTransaction;
import com.digitalwallet.bnkai.exception.AddressNotFoundException;
import com.digitalwallet.bnkai.mapper.PhysicalGoldMapper;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.PhysicalGoldService;
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

@WebMvcTest(PhysicalGoldController.class)
@AutoConfigureMockMvc(addFilters = false)
class PhysicalGoldControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PhysicalGoldService physicalGoldService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @MockitoBean
    private PhysicalGoldMapper physicalGoldMapper;

    @Test
    void buyPhysicalGold_validRequest_returnsPhysicalTransaction() throws Exception {
        BuyPhysicalGoldRequest request = new BuyPhysicalGoldRequest(1, 2, new BigDecimal("2.00"), 3);
        PhysicalGoldTransaction transaction = new PhysicalGoldTransaction();
        transaction.setPhysicalTransactionId(99);
        transaction.setQuantity(new BigDecimal("2.00"));
        PhysicalGoldDTO dto = new PhysicalGoldDTO();
        dto.setPhysicalTransactionId(99);
        dto.setQuantity(new BigDecimal("2.00"));

        when(physicalGoldService.buyPhysicalGold(any(BuyPhysicalGoldRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/physical-gold/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_id").value(99))
                .andExpect(jsonPath("$.quantity").value(2.00));

        verify(physicalGoldService).buyPhysicalGold(any(BuyPhysicalGoldRequest.class));
    }

    @Test
    void buyPhysicalGold_missingDeliveryAddress_returnsBadRequest() throws Exception {
        BuyPhysicalGoldRequest request = new BuyPhysicalGoldRequest(1, 2, new BigDecimal("2.00"), null);

        mockMvc.perform(post("/physical-gold/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Delivery address id is required"));

        verify(physicalGoldService, never()).buyPhysicalGold(any(BuyPhysicalGoldRequest.class));
    }

    @Test
    void convertToPhysicalGold_validRequest_returnsPhysicalTransaction() throws Exception {
        ConvertToPhysicalGoldRequest request = new ConvertToPhysicalGoldRequest(1, 7, new BigDecimal("1.00"), 3);
        PhysicalGoldTransaction transaction = new PhysicalGoldTransaction();
        transaction.setPhysicalTransactionId(100);
        transaction.setQuantity(new BigDecimal("1.00"));
        PhysicalGoldDTO dto = new PhysicalGoldDTO();
        dto.setPhysicalTransactionId(100);
        dto.setQuantity(new BigDecimal("1.00"));

        when(physicalGoldService.convertToPhysicalGold(any(ConvertToPhysicalGoldRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/physical-gold/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transaction_id").value(100))
                .andExpect(jsonPath("$.quantity").value(1.00));

        verify(physicalGoldService).convertToPhysicalGold(any(ConvertToPhysicalGoldRequest.class));
    }

    @Test
    void convertToPhysicalGold_unknownAddress_returnsNotFound() throws Exception {
        ConvertToPhysicalGoldRequest request = new ConvertToPhysicalGoldRequest(1, 7, new BigDecimal("1.00"), 404);
        when(physicalGoldService.convertToPhysicalGold(any(ConvertToPhysicalGoldRequest.class)))
                .thenThrow(new AddressNotFoundException("Delivery address not found"));

        mockMvc.perform(post("/physical-gold/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Delivery address not found"));
    }
}
