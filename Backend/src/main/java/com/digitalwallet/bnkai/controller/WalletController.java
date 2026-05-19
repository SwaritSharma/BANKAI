package com.digitalwallet.bnkai.controller;

import com.digitalwallet.bnkai.dto.WalletTopupRequest;
import com.digitalwallet.bnkai.dto.UserDTO;
import com.digitalwallet.bnkai.mapper.UserMapper;
import com.digitalwallet.bnkai.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final UserMapper userMapper;

    @PostMapping("/topup")
    public ResponseEntity<UserDTO> topupWallet(@Valid @RequestBody WalletTopupRequest request) {
        return ResponseEntity.ok(userMapper.toDto(walletService.topupWallet(request)));
    }
}
