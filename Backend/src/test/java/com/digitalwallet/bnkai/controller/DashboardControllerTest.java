package com.digitalwallet.bnkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitalwallet.bnkai.dto.DashboardDTO;
import com.digitalwallet.bnkai.dto.EditProfileRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.PaymentDTO;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @Test
    void getDashboard_existingUser_returnsDashboardContract() throws Exception {
        DashboardDTO dto = new DashboardDTO();
        dto.setName("Pradeep Kumar");
        dto.setEmail("pradeep.kumar@example.in");
        dto.setBalance(new BigDecimal("20000.00"));
        dto.setTotalHoldingsGrams(new BigDecimal("2.50"));
        dto.setTotalHoldingsValue(new BigDecimal("17875.00"));
        dto.setCurrentGoldPrice(new BigDecimal("7150.00"));
        dto.setPnlAmount(new BigDecimal("2681.25"));
        dto.setPnlPercent(new BigDecimal("15.00"));
        when(dashboardService.getDashboard(1)).thenReturn(dto);

        mockMvc.perform(get("/users/{id}/dashboard", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Pradeep Kumar"))
                .andExpect(jsonPath("$.email").value("pradeep.kumar@example.in"))
                .andExpect(jsonPath("$.total_holdings_grams").value(2.50))
                .andExpect(jsonPath("$.current_gold_price").value(7150.00));
    }

    @Test
    void updateProfile_validRequest_returnsOkAndDelegatesToService() throws Exception {
        EditProfileRequest request = new EditProfileRequest();
        request.setName("Updated User");
        request.setEmail("updated@example.in");
        request.setStreet("123 Main Street");
        request.setCity("Mumbai");
        request.setState("Maharashtra");
        request.setPostalCode("400001");
        request.setCountry("India");

        mockMvc.perform(put("/users/{id}/profile", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(dashboardService).updateProfile(any(Integer.class), any(EditProfileRequest.class));
    }

    @Test
    void updateProfile_invalidEmail_returnsBadRequest() throws Exception {
        EditProfileRequest request = new EditProfileRequest();
        request.setName("Updated User");
        request.setEmail("not-an-email");

        mockMvc.perform(put("/users/{id}/profile", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Invalid email format"));
    }

    @Test
    void getHoldings_existingUser_returnsHoldingArray() throws Exception {
        HoldingDTO holding = new HoldingDTO();
        holding.setHoldingId(5);
        holding.setVendorName("Sona Jewelers");
        holding.setQuantity(new BigDecimal("3.20"));
        holding.setValue(new BigDecimal("22880.00"));
        when(dashboardService.getHoldings(1)).thenReturn(List.of(holding));

        mockMvc.perform(get("/users/{id}/holdings", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].holding_id").value(5))
                .andExpect(jsonPath("$[0].vendor_name").value("Sona Jewelers"))
                .andExpect(jsonPath("$[0].quantity").value(3.20));
    }

    @Test
    void getTransactions_existingUser_returnsTransactionArray() throws Exception {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setTransactionId(11);
        transaction.setTransactionType("Buy");
        transaction.setVendorName("Sona Jewelers");
        transaction.setQuantity(new BigDecimal("2.00"));
        transaction.setAmount(new BigDecimal("14300.00"));
        transaction.setTransactionStatus("Success");
        when(dashboardService.getTransactions(1)).thenReturn(List.of(transaction));

        mockMvc.perform(get("/users/{id}/transactions", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_id").value(11))
                .andExpect(jsonPath("$[0].transaction_type").value("Buy"))
                .andExpect(jsonPath("$[0].transaction_status").value("Success"));
    }

    @Test
    void getAddresses_existingUser_returnsAddressArray() throws Exception {
        HoldingDTO.AddressDTO address = new HoldingDTO.AddressDTO();
        address.setAddressId(3);
        address.setStreet("789 Pine Road");
        address.setCity("Bangalore");
        address.setState("Karnataka");
        address.setPostalCode("560001");
        address.setCountry("India");
        when(dashboardService.getAddresses(1)).thenReturn(List.of(address));

        mockMvc.perform(get("/users/{id}/addresses", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].address_id").value(3))
                .andExpect(jsonPath("$[0].postal_code").value("560001"));
    }

    @Test
    void getPhysicalGold_existingUser_returnsPhysicalGoldArray() throws Exception {
        PhysicalGoldDTO physicalGold = new PhysicalGoldDTO();
        physicalGold.setPhysicalTransactionId(8);
        physicalGold.setVendorName("Golden Heritage");
        physicalGold.setQuantity(new BigDecimal("1.50"));
        when(dashboardService.getPhysicalGold(1)).thenReturn(List.of(physicalGold));

        mockMvc.perform(get("/users/{id}/physical-gold", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_id").value(8))
                .andExpect(jsonPath("$[0].vendor_name").value("Golden Heritage"));
    }

    @Test
    void getPayments_existingUser_returnsPaymentArray() throws Exception {
        PaymentDTO payment = new PaymentDTO();
        payment.setPaymentId(4);
        payment.setPaymentMethod("Bank Transfer");
        payment.setTransactionType("Credited to wallet");
        payment.setPaymentStatus("Success");
        payment.setAmount(new BigDecimal("5000.00"));
        when(dashboardService.getPayments(1)).thenReturn(List.of(payment));

        mockMvc.perform(get("/users/{id}/payments", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].payment_id").value(4))
                .andExpect(jsonPath("$[0].payment_method").value("Bank Transfer"))
                .andExpect(jsonPath("$[0].transaction_type").value("Credited to wallet"));
    }
}
