package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.ledger.LedgerResponse;
import com.vaultpay.vault_pay.entity.JournalEntry;
import com.vaultpay.vault_pay.service.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
    private final LedgerService ledgerService;
    public LedgerController(LedgerService ledgerService){
        this.ledgerService=ledgerService;
    }
    @GetMapping("/accounts/{accountId}")
    public ResponseEntity<List<LedgerResponse>>getAccountHistory(@PathVariable String accountId){
        List<JournalEntry> history=ledgerService.findByAccount(accountId);
        return ResponseEntity.ok(history.stream().map(LedgerResponse::fromEntity).toList());
    }
    @GetMapping("/entries/transaction/{transactionId}")
    public ResponseEntity<LedgerResponse>getEntryByTransaction(@PathVariable Long transactionId){
        JournalEntry entry=ledgerService.findByTransactionGroupId(transactionId);
        return ResponseEntity.ok(LedgerResponse.fromEntity(entry));
    }
}
