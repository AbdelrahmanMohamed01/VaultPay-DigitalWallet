package com.vaultpay.vault_pay.entity;

import jakarta.persistence.*;
import jakarta.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount=BigDecimal.ZERO;
    private LocalDateTime createdAt;
    private String entryType;
    private String description;
    private Long transactionGroupId;
    @JoinColumn(name="account_id")
    @ManyToOne(fetch =FetchType.EAGER)
    private Account account;
    @JoinColumn(name = "transaction_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private TransactionRequest transactionRequest;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getEntryType() {
        return entryType;
    }

    public void setEntryType(String entryType) {
        this.entryType = entryType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTransactionGroupId() {
        return transactionGroupId;
    }

    public void setTransactionGroupId(Long transactionGroupId) {
        this.transactionGroupId = transactionGroupId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public TransactionRequest getTransactionRequest() {
        return transactionRequest;
    }

    public void setTransactionRequest(TransactionRequest transactionRequest) {
        this.transactionRequest = transactionRequest;
    }
}
