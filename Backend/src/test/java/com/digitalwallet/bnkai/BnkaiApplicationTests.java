package com.digitalwallet.bnkai;

import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.repository.VendorBranchRepository;
import com.digitalwallet.bnkai.repository.VendorRepository;
import com.digitalwallet.bnkai.service.VendorDashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@SpringBootTest
class ProjectApplicationTests {

    @Autowired private VendorDashboardService vendorDashboardService;
    @Autowired private VendorRepository vendorRepo;
    @Autowired private VendorBranchRepository branchRepo;

    @Test
    void testFullFlow() {
        System.out.println("STARTING FULL FLOW TEST");
        try {
            Vendor v = new Vendor();
            v.setVendorName("Test Vendor");
            v.setTotalGoldQuantity(BigDecimal.ZERO);
            v.setCurrentGoldPrice(new BigDecimal("7000"));
            v = vendorRepo.save(v);

            VendorBranch b = new VendorBranch();
            b.setVendor(v);
            b.setQuantity(new BigDecimal("10"));
            b = branchRepo.save(b);

            // Add Gold
            vendorDashboardService.addGoldToBranch(v.getVendorId(), b.getBranchId(), new BigDecimal("5.0"));

            // Get Transactions
            var txns = vendorDashboardService.getTransactions(v.getVendorId());
            System.out.println("TRANSACTIONS FETCHED: " + txns.size());
            for (var t : txns) {
                System.out.println("TXN ID: " + t.getTransactionId() + " | TYPE: " + t.getTransactionType() + " | QTY: " + t.getQuantity());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("ENDING FULL FLOW TEST");
    }
}
