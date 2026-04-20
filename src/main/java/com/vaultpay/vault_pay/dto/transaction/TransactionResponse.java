package com.vaultpay.vault_pay.dto.transaction;

import com.vaultpay.vault_pay.entity.TransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionResponse(Long transactionId, String accountNumber, String accountName , BigDecimal amount, String currency,
                                  LocalDateTime createdAt,String status, String checkedBy,LocalDateTime processedAt) {
    public static TransactionResponse fromEntity(TransactionRequest transactionRequest){
        return new TransactionResponse(
                transactionRequest.getId(),
                transactionRequest.getReceiver().getAccountNumber(),
                transactionRequest.getReceiver().getAccountName(),
                transactionRequest.getAmount(),
                transactionRequest.getCurrency(),
                transactionRequest.getCreatedAt(),
                transactionRequest.getStatus(),
                transactionRequest.getChecker().getUsername(),
                transactionRequest.getProcessedAt()
        );
    }
}
