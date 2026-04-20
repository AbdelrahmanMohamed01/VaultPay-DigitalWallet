package com.vaultpay.vault_pay.dto.transaction;

import com.vaultpay.vault_pay.entity.TransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DepositResponse(Long transactionId, String accountNumber, String accountName , BigDecimal amount, String currency, String status,
                              LocalDateTime createdAt) {
    public static DepositResponse fromEntity(TransactionRequest transactionRequest) {
        return new DepositResponse(
                transactionRequest.getId(),
                transactionRequest.getReceiver().getAccountNumber(),
                transactionRequest.getReceiver().getAccountName(),
                transactionRequest.getAmount(),
                transactionRequest.getCurrency(),
                transactionRequest.getStatus(),
                transactionRequest.getCreatedAt()
        );
    }
}
