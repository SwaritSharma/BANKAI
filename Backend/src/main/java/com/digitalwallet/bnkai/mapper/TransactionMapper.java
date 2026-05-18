package com.digitalwallet.bnkai.mapper;

import com.digitalwallet.bnkai.dto.TransactionDTO;
import com.digitalwallet.bnkai.entity.TransactionHistory;
import com.digitalwallet.bnkai.entity.User;
import com.digitalwallet.bnkai.entity.VendorBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "vendorName", source = "transaction", qualifiedByName = "vendorName")
    @Mapping(target = "branchName", source = "transaction", qualifiedByName = "branchName")
    @Mapping(target = "branchAddress", source = "transaction", qualifiedByName = "branchAddress")
    @Mapping(target = "userName", source = "transaction", qualifiedByName = "userName")
    @Mapping(target = "userAddress", source = "transaction", qualifiedByName = "userAddress")
    TransactionDTO toDto(TransactionHistory transaction);

    List<TransactionDTO> toDtoList(List<TransactionHistory> transactions);

    @Mapping(target = "transactionId", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "branch", source = "branch")
    @Mapping(target = "quantity", source = "quantity")
    @Mapping(target = "amount", source = "amount")
    @Mapping(target = "transactionType", source = "transactionType")
    @Mapping(target = "transactionStatus", source = "transactionStatus")
    @Mapping(target = "createdAt", source = "createdAt")
    TransactionHistory toEntity(
            User user,
            VendorBranch branch,
            BigDecimal quantity,
            BigDecimal amount,
            String transactionType,
            String transactionStatus,
            LocalDateTime createdAt
    );

    @Named("vendorName")
    default String vendorName(TransactionHistory transaction) {
        if (transaction == null || transaction.getBranch() == null || transaction.getBranch().getVendor() == null) {
            return "Unknown Vendor";
        }
        return transaction.getBranch().getVendor().getVendorName();
    }

    @Named("branchName")
    default String branchName(TransactionHistory transaction) {
        if (transaction == null || transaction.getBranch() == null || transaction.getBranch().getBranchId() == null) {
            return "";
        }
        return "Branch #" + transaction.getBranch().getBranchId();
    }

    @Named("branchAddress")
    default String branchAddress(TransactionHistory transaction) {
        if (transaction == null || transaction.getBranch() == null || transaction.getBranch().getAddress() == null) {
            return "";
        }
        String city = transaction.getBranch().getAddress().getCity();
        String state = transaction.getBranch().getAddress().getState();
        if (city == null && state == null) {
            return "";
        }
        if (city == null) {
            return state;
        }
        if (state == null) {
            return city;
        }
        return city + ", " + state;
    }

    @Named("userName")
    default String userName(TransactionHistory transaction) {
        if (transaction == null || transaction.getUser() == null) {
            return "Vendor Action";
        }
        return transaction.getUser().getName();
    }

    @Named("userAddress")
    default String userAddress(TransactionHistory transaction) {
        if (transaction == null || transaction.getUser() == null || transaction.getUser().getAddress() == null) {
            return "";
        }
        String city = transaction.getUser().getAddress().getCity();
        String state = transaction.getUser().getAddress().getState();
        if (city == null && state == null) {
            return "";
        }
        if (city == null) {
            return state;
        }
        if (state == null) {
            return city;
        }
        return city + ", " + state;
    }
}
