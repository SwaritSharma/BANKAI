package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.dto.WalletTopupRequest;
import com.digitalwallet.bnkai.entity.User;

public interface WalletService {

    User topupWallet(
            WalletTopupRequest request
    );
}