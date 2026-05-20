package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.BuyPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.ConvertToPhysicalGoldRequest;
import com.digitalwallet.bnkai.dto.PhysicalGoldDTO;
import com.digitalwallet.bnkai.service.PhysicalGoldService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/physical-gold")
@RequiredArgsConstructor
public class PhysicalGoldController {

    private final PhysicalGoldService physicalGoldService;

    @PostMapping("/buy")
    public ResponseEntity<PhysicalGoldDTO> buyPhysicalGold(@Valid @RequestBody BuyPhysicalGoldRequest request) {
        return ResponseEntity.ok(physicalGoldService.buyPhysicalGold(request));
    }

    @PostMapping("/convert")
    public ResponseEntity<PhysicalGoldDTO> convertToPhysicalGold(@Valid @RequestBody ConvertToPhysicalGoldRequest request) {
        return ResponseEntity.ok(physicalGoldService.convertToPhysicalGold(request));
    }
}
