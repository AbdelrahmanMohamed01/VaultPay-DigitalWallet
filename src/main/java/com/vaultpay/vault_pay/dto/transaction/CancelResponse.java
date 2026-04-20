package com.vaultpay.vault_pay.dto.transaction;

import com.vaultpay.vault_pay.entity.TransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CancelResponse(Long id , String accountNumber, String accountName, BigDecimal amount, String currency , String status,
                             LocalDateTime createdAt,LocalDateTime processedAt) {
    public static CancelResponse fromEntity(TransactionRequest transactionRequest){
        return new CancelResponse(
                transactionRequest.getId(),
                transactionRequest.getReceiver().getAccountNumber(),
                transactionRequest.getReceiver().getAccountName(),
                transactionRequest.getAmount(),
                transactionRequest.getCurrency(),
                transactionRequest.getStatus(),
                transactionRequest.getCreatedAt(),
                transactionRequest.getProcessedAt()
        );
    }
}
