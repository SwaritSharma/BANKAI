package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.GoldPriceHistoryDTO;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.GoldPriceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GoldPriceController.class)
@AutoConfigureMockMvc(addFilters = false)
class GoldPriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GoldPriceService goldPriceService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @Test
    void getCurrentPrice_serviceReturnsPrice_returnsGoldPriceJson() throws Exception {
        GoldPriceDTO response = new GoldPriceDTO();
        response.setPrice(new BigDecimal("7150.00"));
        response.setChange24h(new BigDecimal("45.50"));
        response.setChangePct(new BigDecimal("0.64"));

        when(goldPriceService.getCurrentPrice()).thenReturn(response);

        mockMvc.perform(get("/gold/price"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(7150.00))
                .andExpect(jsonPath("$.change_24h").value(45.50))
                .andExpect(jsonPath("$.change_pct").value(0.64));

        verify(goldPriceService).getCurrentPrice();
    }

    @Test
    void getPriceHistory_daysQueryParam_returnsRequestedHistory() throws Exception {
        GoldPriceHistoryDTO dayOne = new GoldPriceHistoryDTO();
        dayOne.setDate("2026-05-17");
        dayOne.setPrice(new BigDecimal("7100.00"));
        GoldPriceHistoryDTO dayTwo = new GoldPriceHistoryDTO();
        dayTwo.setDate("2026-05-18");
        dayTwo.setPrice(new BigDecimal("7150.00"));

        when(goldPriceService.getPriceHistory(2)).thenReturn(List.of(dayOne, dayTwo));

        mockMvc.perform(get("/gold/price-history").param("days", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-05-17"))
                .andExpect(jsonPath("$[0].price").value(7100.00))
                .andExpect(jsonPath("$[1].date").value("2026-05-18"))
                .andExpect(jsonPath("$[1].price").value(7150.00));

        verify(goldPriceService).getPriceHistory(2);
    }
}
