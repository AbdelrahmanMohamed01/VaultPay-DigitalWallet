package com.vaultpay.vault_pay.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class TransactionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount=BigDecimal.ZERO;
    private String currency;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String rejectionReason;
    private String idempotencyKey;
    @JoinColumn(name = "sender_account_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Account sender;
    @JoinColumn(name = "receiver_account_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private Account receiver;
    @JoinColumn(name = "maker_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private User maker;
    @JoinColumn(name = "checker_id")
    @ManyToOne(fetch = FetchType.EAGER)
    private User checker;

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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setProcessedAt(LocalDateTime processedAt){
        this.processedAt=processedAt;
    }

    public LocalDateTime getProcessedAt(){
        return processedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public Account getSender() {
        return sender;
    }

    public void setSender(Account sender) {
        this.sender = sender;
    }

    public Account getReceiver() {
        return receiver;
    }

    public void setReceiver(Account receiver) {
        this.receiver = receiver;
    }

    public User getMaker() {
        return maker;
    }

    public void setMaker(User maker) {
        this.maker = maker;
    }

    public User getChecker() {
        return checker;
    }

    public void setChecker(User checker) {
        this.checker = checker;
    }
}
