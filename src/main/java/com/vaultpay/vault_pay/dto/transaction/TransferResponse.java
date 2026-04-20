package com.vaultpay.vault_pay.dto.transaction;

import com.vaultpay.vault_pay.entity.TransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferResponse(Long transactionId, String senderAccountName, String senderAccountNumber, String receiverAccountName, String receiverAccountNumber, BigDecimal amount, String currency,
                               LocalDateTime createdAt) {
     public static TransferResponse fromEntity(TransactionRequest transactionRequest){
        return new TransferResponse(
                transactionRequest.getId(),
                transactionRequest.getSender().getAccountNumber(),
                transactionRequest.getSender().getAccountName(),
                transactionRequest.getReceiver().getAccountNumber(),
                transactionRequest.getReceiver().getAccountName(),
                transactionRequest.getAmount(),
                transactionRequest.getCurrency(),
                transactionRequest.getCreatedAt()
        );
    }
}
