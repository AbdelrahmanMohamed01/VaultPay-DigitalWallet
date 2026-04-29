package com.vaultpay.vault_pay.service;

import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.repository.AccountRepository;
import com.vaultpay.vault_pay.repository.TransactionRequestRepository;
import com.vaultpay.vault_pay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionQueryService {
    private final TransactionRequestRepository transactionRequestRepository;
    @Value("${vaultpay.limits.high-value-threshold}")
    private BigDecimal highValueThreshold;
    TransactionQueryService(TransactionRequestRepository transactionRequestRepository){
        this.transactionRequestRepository=transactionRequestRepository;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionRequest findTransactionById(Long transactionId) {
        return transactionRequestRepository.findById(transactionId).orElse(null);
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<TransactionRequest> findTransactionsByUsername(String username) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverUsername(username);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderUsername(username);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<TransactionRequest> findTransactionsByAccountNumber(String accountNumber) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverAccountNumber(accountNumber);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderAccountNumber(accountNumber);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public List<TransactionRequest> findTransactionsByAccountNumberAndStatus(String accountNumber, String status) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverAccountNumberAndStatus(accountNumber,status);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderAccountNumberAndStatus(accountNumber,status);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }

    @PreAuthorize("hasRole('MANAGER')")
    public List<TransactionRequest> getHighValueTransactions() {
        return transactionRequestRepository.findByAmountGreaterThanEqual(highValueThreshold);
    }
}
