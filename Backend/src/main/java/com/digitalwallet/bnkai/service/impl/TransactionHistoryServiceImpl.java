package com.digitalwallet.bnkai.service.impl;

import com.digitalwallet.bnkai.service.TransactionHistoryService;

import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.mapper.TransactionMapper;
import com.digitalwallet.bnkai.repository.TransactionHistoryRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionHistoryServiceImpl
        implements TransactionHistoryService {

    private final
    TransactionHistoryRepository
            transactionHistoryRepository;

    private final
    TransactionMapper
            transactionMapper;

    public TransactionHistoryServiceImpl(
            TransactionHistoryRepository
                    transactionHistoryRepository,
            TransactionMapper
                    transactionMapper
    ) {

        this.transactionHistoryRepository =
                transactionHistoryRepository;

        this.transactionMapper =
                transactionMapper;
    }

    @Override
    public TransactionHistory
    createBuyTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    ) {

        TransactionHistory transactionHistory = transactionMapper.toEntity(
                user,
                branch,
                quantity,
                amount,
                TransactionConstants.BUY,
                transactionStatus,
                LocalDateTime.now()
        );

        return transactionHistoryRepository
                .save(transactionHistory);
    }

    @Override
    public TransactionHistory
    createSellTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    ) {

        TransactionHistory transactionHistory = transactionMapper.toEntity(
                user,
                branch,
                quantity,
                amount,
                TransactionConstants.SELL,
                transactionStatus,
                LocalDateTime.now()
        );

        return transactionHistoryRepository
                .save(transactionHistory);
    }

    @Override
    public TransactionHistory
    createConvertToPhysicalTransaction(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionStatus
    ) {

        TransactionHistory transactionHistory = transactionMapper.toEntity(
                user,
                branch,
                quantity,
                amount,
                TransactionConstants.CONVERT_TO_PHYSICAL,
                transactionStatus,
                LocalDateTime.now()
        );

        return transactionHistoryRepository
                .save(transactionHistory);
    }
}
