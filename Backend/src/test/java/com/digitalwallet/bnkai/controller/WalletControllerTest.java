package com.digitalwallet.bnkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitalwallet.bnkai.dto.UserDTO;
import com.digitalwallet.bnkai.dto.WalletTopupRequest;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.exception.InvalidQuantityException;
import com.digitalwallet.bnkai.mapper.UserMapper;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.WalletService;
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

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private WalletService walletService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @MockitoBean
    private UserMapper userMapper;

    @Test
    void topupWallet_validRequest_returnsUpdatedUserBalance() throws Exception {
        WalletTopupRequest request = new WalletTopupRequest(1, new BigDecimal("500.00"), "Bank Transfer");
        User user = new User();
        user.setUserId(1);
        user.setName("Pradeep Kumar");
        user.setEmail("pradeep.kumar@example.in");
        user.setBalance(new BigDecimal("1500.00"));
        UserDTO dto = new UserDTO();
        dto.setUserId(1);
        dto.setName("Pradeep Kumar");
        dto.setEmail("pradeep.kumar@example.in");
        dto.setBalance(new BigDecimal("1500.00"));

        when(walletService.topupWallet(any(WalletTopupRequest.class))).thenReturn(dto);

        mockMvc.perform(post("/wallet/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_id").value(1))
                .andExpect(jsonPath("$.email").value("pradeep.kumar@example.in"))
                .andExpect(jsonPath("$.balance").value(1500.00));

        verify(walletService).topupWallet(any(WalletTopupRequest.class));
    }

    @Test
    void topupWallet_invalidAmount_returnsBadRequestErrorStructure() throws Exception {
        WalletTopupRequest request = new WalletTopupRequest(1, BigDecimal.ZERO, "Bank Transfer");

        mockMvc.perform(post("/wallet/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Amount must be greater than 0"));

        verify(walletService, never()).topupWallet(any(WalletTopupRequest.class));
    }

    @Test
    void topupWallet_serviceRejectsAmount_returnsBadRequest() throws Exception {
        WalletTopupRequest request = new WalletTopupRequest(1, new BigDecimal("1.00"), "Bank Transfer");
        when(walletService.topupWallet(any(WalletTopupRequest.class)))
                .thenThrow(new InvalidQuantityException("Amount must be greater than 0"));

        mockMvc.perform(post("/wallet/topup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Amount must be greater than 0"))
                .andExpect(jsonPath("$.details[0]").value("Amount must be greater than 0"));
    }

    @Test
    void topupWallet_missingBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/wallet/topup").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
