package com.vaultpay.vault_pay.dto.ledger;

import com.vaultpay.vault_pay.entity.JournalEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LedgerResponse(Long entryId, String accountNumber, String type, BigDecimal amount, BigDecimal balanceAfter, String transactionGroupId,
                             LocalDateTime timestamp) {
    public static LedgerResponse fromEntity(JournalEntry journalEntry){
        return new LedgerResponse(journalEntry.getId(),
                journalEntry.getAccount().getAccountNumber(),
                journalEntry.getEntryType(),
                journalEntry.getAmount(),
                journalEntry.getAccount().getCurrentBalance(),
                journalEntry.getTransactionGroupId(),
                journalEntry.getCreatedAt());
    }
}
