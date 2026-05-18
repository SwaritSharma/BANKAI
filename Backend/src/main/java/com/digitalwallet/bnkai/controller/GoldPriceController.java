package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.GoldPriceDTO;
import com.digitalwallet.bnkai.dto.GoldPriceHistoryDTO;
import com.digitalwallet.bnkai.service.GoldPriceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping("/gold")
@RequiredArgsConstructor
public class GoldPriceController {

    private final GoldPriceService goldPriceService;

    @GetMapping("/price")
    public GoldPriceDTO getCurrentPrice() {
        return goldPriceService.getCurrentPrice();
    }

    @GetMapping("/price-history")
    public List<GoldPriceHistoryDTO> getPriceHistory(
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "History days must be at least 1")
            @Max(value = 365, message = "History days must be at most 365")
            int days
    ) {
        return goldPriceService.getPriceHistory(days);
    }
}
