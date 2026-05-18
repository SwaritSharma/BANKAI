package com.digitalwallet.bnkai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitalwallet.bnkai.dto.AddBranchRequest;
import com.digitalwallet.bnkai.dto.AddGoldRequest;
import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.dto.VendorBranchDTO;
import com.digitalwallet.bnkai.dto.VendorDashboardDTO;
import com.digitalwallet.bnkai.dto.VendorDTO;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.mapper.VendorMapper;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.security.jwt.JwtService;
import com.digitalwallet.bnkai.security.service.CustomUserDetailsService;
import com.digitalwallet.bnkai.security.service.VendorUserDetailsService;
import com.digitalwallet.bnkai.service.GoldPriceService;
import com.digitalwallet.bnkai.service.VendorDashboardService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VendorController.class)
@AutoConfigureMockMvc(addFilters = false)
class VendorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VendorRepository vendorRepository;

    @MockitoBean
    private GoldPriceService goldPriceService;

    @MockitoBean
    private VendorDashboardService vendorDashboardService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private VendorUserDetailsService vendorUserDetailsService;

    @MockitoBean
    private VendorMapper vendorMapper;

    @Test
    void getVendors_existingVendors_returnsFrontendVendorContract() throws Exception {
        Vendor vendor = new Vendor();
        vendor.setVendorId(1);
        vendor.setVendorName("Sona Jewelers");
        vendor.setCurrentGoldPrice(new BigDecimal("6400.00"));
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setVendorId(1);
        vendorDTO.setVendorName("Sona Jewelers");
        vendorDTO.setCurrentGoldPrice(new BigDecimal("6400.00"));
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));
        when(vendorMapper.toDto(any(Vendor.class), any(BigDecimal.class))).thenReturn(vendorDTO);

        mockMvc.perform(get("/vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].vendor_id").value(1))
                .andExpect(jsonPath("$[0].vendor_name").value("Sona Jewelers"))
                .andExpect(jsonPath("$[0].current_gold_price").value(6400.00));
    }

    @Test
    void getVendors_missingVendorPrice_usesGoldPriceServiceFallback() throws Exception {
        Vendor vendor = new Vendor();
        vendor.setVendorId(2);
        vendor.setVendorName("Golden Heritage");
        VendorDTO vendorDTO = new VendorDTO();
        vendorDTO.setVendorId(2);
        vendorDTO.setVendorName("Golden Heritage");
        vendorDTO.setCurrentGoldPrice(new BigDecimal("7150.00"));
        GoldPriceDTO price = new GoldPriceDTO();
        price.setPrice(new BigDecimal("7150.00"));
        when(vendorRepository.findAll()).thenReturn(List.of(vendor));
        when(goldPriceService.getCurrentPrice()).thenReturn(price);
        when(vendorMapper.toDto(any(Vendor.class), any(BigDecimal.class))).thenReturn(vendorDTO);

        mockMvc.perform(get("/vendors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].current_gold_price").value(7150.00));
    }

    @Test
    void getDashboard_existingVendor_returnsDashboard() throws Exception {
        VendorDashboardDTO dto = new VendorDashboardDTO();
        dto.setVendorId(1);
        dto.setVendorName("Sona Jewelers");
        dto.setTotalBranches(2);
        dto.setTotalGoldQuantity(new BigDecimal("250.00"));
        when(vendorDashboardService.getDashboard(1)).thenReturn(dto);

        mockMvc.perform(get("/vendors/{id}/dashboard", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor_id").value(1))
                .andExpect(jsonPath("$.vendor_name").value("Sona Jewelers"))
                .andExpect(jsonPath("$.total_branches").value(2));
    }

    @Test
    void updateProfile_validRequest_returnsUpdatedDashboard() throws Exception {
        VendorDashboardDTO dto = new VendorDashboardDTO();
        dto.setVendorId(1);
        dto.setContactPersonName("Rohit Verma");
        when(vendorDashboardService.updateProfile(any(Integer.class), any())).thenReturn(dto);

        mockMvc.perform(put("/vendors/{id}/profile", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"contactPersonName\":\"Rohit Verma\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vendor_id").value(1))
                .andExpect(jsonPath("$.contact_person_name").value("Rohit Verma"));
    }

    @Test
    void getBranches_existingVendor_returnsBranches() throws Exception {
        VendorBranchDTO branch = new VendorBranchDTO();
        branch.setBranchId(9);
        branch.setQuantity(new BigDecimal("100.00"));
        HoldingDTO.AddressDTO address = new HoldingDTO.AddressDTO();
        address.setCity("Mumbai");
        branch.setAddress(address);
        when(vendorDashboardService.getBranches(1)).thenReturn(List.of(branch));

        mockMvc.perform(get("/vendors/{id}/branches", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branch_id").value(9))
                .andExpect(jsonPath("$[0].address.city").value("Mumbai"));
    }

    @Test
    void addBranch_validRequest_returnsCreatedBranch() throws Exception {
        AddBranchRequest request = new AddBranchRequest();
        request.setStreet("123 Main Street");
        request.setCity("Mumbai");
        request.setState("Maharashtra");
        request.setPostalCode("400001");
        request.setCountry("India");
        request.setInitialQuantity(new BigDecimal("25.00"));
        VendorBranchDTO branch = new VendorBranchDTO();
        branch.setBranchId(3);
        branch.setQuantity(new BigDecimal("25.00"));
        when(vendorDashboardService.addBranch(any(Integer.class), any(AddBranchRequest.class))).thenReturn(branch);

        mockMvc.perform(post("/vendors/{id}/branches", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.branch_id").value(3))
                .andExpect(jsonPath("$.quantity").value(25.00));
    }

    @Test
    void addBranch_missingCity_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/vendors/{id}/branches", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"123 Main Street\",\"state\":\"Maharashtra\",\"postal_code\":\"400001\",\"country\":\"India\",\"initial_quantity\":25}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("City is required"));
    }

    @Test
    void deleteBranch_existingBranch_returnsOk() throws Exception {
        mockMvc.perform(delete("/vendors/{id}/branches/{branchId}", 1, 3))
                .andExpect(status().isNoContent());

        verify(vendorDashboardService).deleteBranch(1, 3);
    }

    @Test
    void getTransactions_existingVendor_returnsTransactions() throws Exception {
        TransactionDTO transaction = new TransactionDTO();
        transaction.setTransactionId(5);
        transaction.setTransactionType("Add Inventory");
        transaction.setTransactionStatus("Success");
        when(vendorDashboardService.getTransactions(1)).thenReturn(List.of(transaction));

        mockMvc.perform(get("/vendors/{id}/transactions", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transaction_id").value(5))
                .andExpect(jsonPath("$[0].transaction_type").value("Add Inventory"));
    }

    @Test
    void addGold_validRequest_returnsOk() throws Exception {
        AddGoldRequest request = new AddGoldRequest();
        request.setBranchId(3);
        request.setQuantity(new BigDecimal("10.00"));

        mockMvc.perform(post("/vendors/{id}/add-gold", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(vendorDashboardService).addGoldToBranch(1, 3, new BigDecimal("10.00"));
    }

    @Test
    void addGold_negativeQuantity_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/vendors/{id}/add-gold", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"branchId\":3,\"quantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details[0]").value("Quantity must be positive"));
    }
}
