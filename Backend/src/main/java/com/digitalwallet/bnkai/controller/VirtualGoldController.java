package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.BuyVirtualGoldRequest;
import com.digitalwallet.bnkai.dto.HoldingDTO;
import com.digitalwallet.bnkai.dto.SellVirtualGoldRequest;
import com.digitalwallet.bnkai.mapper.HoldingMapper;
import com.digitalwallet.bnkai.service.GoldPriceService;
import com.digitalwallet.bnkai.service.VirtualGoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/virtual-gold")
@RequiredArgsConstructor
public class VirtualGoldController {

    private final VirtualGoldService virtualGoldService;
    private final HoldingMapper holdingMapper;
    private final GoldPriceService goldPriceService;

    @PostMapping("/buy")
    public ResponseEntity<HoldingDTO> buyVirtualGold(@Valid @RequestBody BuyVirtualGoldRequest request) {
        return ResponseEntity.ok(holdingMapper.toDto(
                virtualGoldService.buyVirtualGold(request),
                goldPriceService.getCurrentPrice().getPrice()
        ));
    }

    @PostMapping("/sell")
    public ResponseEntity<HoldingDTO> sellVirtualGold(@Valid @RequestBody SellVirtualGoldRequest request) {
        return ResponseEntity.ok(holdingMapper.toDto(
                virtualGoldService.sellVirtualGold(request),
                goldPriceService.getCurrentPrice().getPrice()
        ));
    }
}
