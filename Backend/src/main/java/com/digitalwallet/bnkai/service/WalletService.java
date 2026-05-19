package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.UserDTO;
import com.digitalwallet.bnkai.dto.WalletTopupRequest;

public interface WalletService {

    UserDTO topupWallet(
            WalletTopupRequest request
    );
}