package com.digitalwallet.bnkai.service;

import com.digitalwallet.bnkai.constants.TransactionConstants;
import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.Vendor;
import com.digitalwallet.bnkai.entity.VendorBranch;
import com.digitalwallet.bnkai.mapper.TransactionMapper;
import com.digitalwallet.bnkai.repository.TransactionHistoryRepository;
import com.digitalwallet.bnkai.service.impl.TransactionHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionHistoryServiceImplTest {

    @Mock
    private TransactionHistoryRepository
            transactionHistoryRepository;

    @Mock
    private TransactionMapper
            transactionMapper;

    @InjectMocks
    private TransactionHistoryServiceImpl
            transactionHistoryService;

    private User user;

    private Vendor vendor;

    private VendorBranch branch;

    @BeforeEach
    void setUp() {

        user = new User();

        user.setUserId(1);

        vendor = new Vendor();

        vendor.setVendorId(1);

        branch = new VendorBranch();

        branch.setBranchId(1);

        branch.setVendor(vendor);
    }

    @Test
    void createBuyTransaction_ShouldCreateSuccessfully() {

        stubTransactionMapper();

        when(
                transactionHistoryRepository.save(
                        any(TransactionHistory.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TransactionHistory transaction =
                transactionHistoryService
                        .createBuyTransaction(
                                user,
                                branch,
                                new BigDecimal("1"),
                                new BigDecimal("5000"),
                                TransactionConstants.SUCCESS
                        );

        assertNotNull(transaction);

        assertEquals(
                user,
                transaction.getUser()
        );

        assertEquals(
                branch,
                transaction.getBranch()
        );

        assertEquals(
                0,
                new BigDecimal("1")
                        .compareTo(
                                transaction.getQuantity()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(
                                transaction.getAmount()
                        )
        );

        assertEquals(
                TransactionConstants.BUY,
                transaction.getTransactionType()
        );

        assertEquals(
                TransactionConstants.SUCCESS,
                transaction.getTransactionStatus()
        );

        assertNotNull(
                transaction.getCreatedAt()
        );

        verify(transactionHistoryRepository)
                .save(
                        any(TransactionHistory.class)
                );
    }

    @Test
    void createSellTransaction_ShouldCreateSuccessfully() {

        stubTransactionMapper();

        when(
                transactionHistoryRepository.save(
                        any(TransactionHistory.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TransactionHistory transaction =
                transactionHistoryService
                        .createSellTransaction(
                                user,
                                branch,
                                new BigDecimal("2"),
                                new BigDecimal("10000"),
                                TransactionConstants.SUCCESS
                        );

        assertNotNull(transaction);

        assertEquals(
                user,
                transaction.getUser()
        );

        assertEquals(
                branch,
                transaction.getBranch()
        );

        assertEquals(
                0,
                new BigDecimal("2")
                        .compareTo(
                                transaction.getQuantity()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("10000")
                        .compareTo(
                                transaction.getAmount()
                        )
        );

        assertEquals(
                TransactionConstants.SELL,
                transaction.getTransactionType()
        );

        assertEquals(
                TransactionConstants.SUCCESS,
                transaction.getTransactionStatus()
        );

        assertNotNull(
                transaction.getCreatedAt()
        );

        verify(transactionHistoryRepository)
                .save(
                        any(TransactionHistory.class)
                );
    }

    @Test
    void createConvertToPhysicalTransaction_ShouldCreateSuccessfully() {

        stubTransactionMapper();

        when(
                transactionHistoryRepository.save(
                        any(TransactionHistory.class)
                )
        ).thenAnswer(
                invocation ->
                        invocation.getArgument(0)
        );

        TransactionHistory transaction =
                transactionHistoryService
                        .createConvertToPhysicalTransaction(
                                user,
                                branch,
                                new BigDecimal("1"),
                                new BigDecimal("5000"),
                                TransactionConstants.SUCCESS
                        );

        assertNotNull(transaction);

        assertEquals(
                user,
                transaction.getUser()
        );

        assertEquals(
                branch,
                transaction.getBranch()
        );

        assertEquals(
                0,
                new BigDecimal("1")
                        .compareTo(
                                transaction.getQuantity()
                        )
        );

        assertEquals(
                0,
                new BigDecimal("5000")
                        .compareTo(
                                transaction.getAmount()
                        )
        );

        assertEquals(
                TransactionConstants.CONVERT_TO_PHYSICAL,
                transaction.getTransactionType()
        );

        assertEquals(
                TransactionConstants.SUCCESS,
                transaction.getTransactionStatus()
        );

        assertNotNull(
                transaction.getCreatedAt()
        );

        verify(transactionHistoryRepository)
                .save(
                        any(TransactionHistory.class)
                );
    }

    private void stubTransactionMapper() {
        when(
                transactionMapper.toEntity(
                        any(),
                        any(),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        anyString(),
                        anyString(),
                        any()
                )
        ).thenAnswer(invocation -> new TransactionHistory(
                null,
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2),
                invocation.getArgument(3),
                invocation.getArgument(4),
                invocation.getArgument(5),
                invocation.getArgument(6)
        ));
    }
}
