package com.vaultpay.vault_pay.service;

import com.vaultpay.vault_pay.entity.Account;
import com.vaultpay.vault_pay.entity.JournalEntry;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.repository.AccountRepository;
import com.vaultpay.vault_pay.repository.JournalEntryRepository;
import com.vaultpay.vault_pay.repository.TransactionRequestRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class LedgerService {
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final TransactionRequestRepository transactionRequestRepository;
    LedgerService(JournalEntryRepository journalEntryRepository,AccountRepository accountRepository,TransactionRequestRepository transactionRequestRepository){
        this.journalEntryRepository=journalEntryRepository;
        this.accountRepository=accountRepository;
        this.transactionRequestRepository=transactionRequestRepository;
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<JournalEntry> findByAccount(String accountId) {
        return journalEntryRepository.findByAccountNumberOrderByCreatedAtDesc(accountId);
    }
    @PreAuthorize("hasRole('ADMIN')")
    public JournalEntry findByTransactionGroupId(Long transactionId) {
        return journalEntryRepository.findByTransactionGroupId(transactionId);
    }
    public void addDepositEntry(String receiverAccountNumber, BigDecimal amount,Long transactionId){
        String transactionGroupId= String.valueOf(UUID.randomUUID());

        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElse(null);

        JournalEntry debitEntry=new JournalEntry();
        debitEntry.setTransactionGroupId(transactionGroupId);
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(amount);
        Account receiverAccount=accountRepository.findByAccountNumber(receiverAccountNumber);
        debitEntry.setAccount(receiverAccount);
        debitEntry.setTransactionRequest(transactionRequest);
        debitEntry.setDescription("deposit transaction");
        debitEntry.setCreatedAt(LocalDateTime.now());
        journalEntryRepository.save(debitEntry);

    }
    public void addTransferEntry(String senderAccountNumber, String receiverAccountNumber, BigDecimal amountSent ,BigDecimal amountReceived ,Long transactionId){
        String transactionGroupId= String.valueOf(UUID.randomUUID());

        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElse(null);

        JournalEntry debitEntry=new JournalEntry();
        debitEntry.setTransactionGroupId(transactionGroupId);
        debitEntry.setEntryType("DEBIT");
        debitEntry.setAmount(amountSent);
        Account senderAccount=accountRepository.findByAccountNumber(senderAccountNumber);
        debitEntry.setAccount(senderAccount);
        debitEntry.setTransactionRequest(transactionRequest);
        debitEntry.setDescription("transfer transaction");
        debitEntry.setCreatedAt(LocalDateTime.now());
        journalEntryRepository.save(debitEntry);

        JournalEntry creditEntry=new JournalEntry();
        creditEntry.setTransactionGroupId(transactionGroupId);
        creditEntry.setEntryType("CREDIT");
        creditEntry.setAmount(amountReceived);
        Account receiverAccount=accountRepository.findByAccountNumber(receiverAccountNumber);
        creditEntry.setAccount(receiverAccount);
        creditEntry.setTransactionRequest(transactionRequest);
        creditEntry.setDescription("transfer transaction");
        creditEntry.setCreatedAt(LocalDateTime.now());
        journalEntryRepository.save(creditEntry);
    }
}
