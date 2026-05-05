package com.vaultpay.vault_pay.service;

import com.vaultpay.vault_pay.dto.transaction.DepositRequest;
import com.vaultpay.vault_pay.dto.transaction.RejectRequest;
import com.vaultpay.vault_pay.dto.transaction.TransactionResponse;
import com.vaultpay.vault_pay.dto.transaction.TransferRequest;
import com.vaultpay.vault_pay.entity.Account;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.entity.User;
import com.vaultpay.vault_pay.repository.AccountRepository;
import com.vaultpay.vault_pay.repository.TransactionRequestRepository;
import com.vaultpay.vault_pay.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    private final AccountRepository accountRepository;
    private final TransactionRequestRepository transactionRequestRepository;
    private final UserRepository userRepository;
    private final CurrencyConverter currencyConverter;
    private final LedgerService ledgerService;

    TransactionService(AccountRepository accountRepository,TransactionRequestRepository transactionRequestRepository,UserRepository userRepository,CurrencyConverter currencyConverter,LedgerService ledgerService){
        this.accountRepository=accountRepository;
        this.transactionRequestRepository=transactionRequestRepository;
        this.userRepository=userRepository;
        this.currencyConverter=currencyConverter;
        this.ledgerService=ledgerService;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionRequest processDeposit(DepositRequest depositRequest, String username, String idempotencyKey) {
        TransactionRequest existingTransactionRequest=transactionRequestRepository.findByIdempotencyKey(idempotencyKey);
        if(existingTransactionRequest!=null){
            return existingTransactionRequest;
        }
        TransactionRequest transactionRequest=new TransactionRequest();
        Account receiver=accountRepository.findByAccountNumber(depositRequest.accountNumber());
        if(!receiver.getStatus().equals("ACTIVE")){
            throw new RuntimeException("cannot perform deposit operation to a locked account");
        }

        User maker=userRepository.findByUsername(username);
        transactionRequest.setMaker(maker);
        //no checker

        //deposit no sender
        transactionRequest.setSender(null);
        transactionRequest.setReceiver(receiver);
        transactionRequest.setCurrency(depositRequest.currency());
        transactionRequest.setAmount(depositRequest.amount());
        transactionRequest.setStatus("PENDING");
        transactionRequest.setCreatedAt(LocalDateTime.now());
        transactionRequest.setIdempotencyKey(idempotencyKey);
        TransactionRequest dbTransactionRequest= transactionRequestRepository.save(transactionRequest);

        ledgerService.addDepositEntry(receiver.getAccountNumber(),depositRequest.amount(),dbTransactionRequest.getId());

        return dbTransactionRequest;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionRequest processTransfer(TransferRequest transferRequest, String idempotencyKey){
        TransactionRequest existingTransactionRequest=transactionRequestRepository.findByIdempotencyKey(idempotencyKey);
        if(existingTransactionRequest!=null){
            return existingTransactionRequest;
        }

        Account senderAccount=accountRepository.findByAccountNumber(transferRequest.senderAccountNumber());
        Account receiverAccount=accountRepository.findByAccountNumber(transferRequest.receiverAccountNumber());


        if(!senderAccount.getStatus().equals("ACTIVE")){
            throw new RuntimeException("cannot perform transafer operation to a locked account");
        }

        if(!receiverAccount.getStatus().equals("ACTIVE")){
            throw new RuntimeException("cannot send money to a locked account");
        }

        BigDecimal amountSent=transferRequest.amount();

        BigDecimal amountReceived=amountSent;
        if(!senderAccount.getCurrency().equals(receiverAccount.getCurrency())){
            amountReceived=currencyConverter.covert(amountReceived,senderAccount.getCurrency(),receiverAccount.getCurrency());
        }

        // Check if sender has enough balance
        if (senderAccount.getCurrentBalance().compareTo(amountSent) < 0) {
            throw new RuntimeException("Insufficient funds for this transfer");
        }

        deposit(receiverAccount,amountReceived);
        deduct(senderAccount,amountSent);

        TransactionRequest transactionRequest=new TransactionRequest();
        transactionRequest.setAmount(amountSent);
        transactionRequest.setCurrency(transferRequest.currency());
        transactionRequest.setCreatedAt(LocalDateTime.now());
        transactionRequest.setSender(senderAccount);
        transactionRequest.setReceiver(receiverAccount);
        transactionRequest.setIdempotencyKey(idempotencyKey);
        TransactionRequest dbTransactionRequest= transactionRequestRepository.save(transactionRequest);

        ledgerService.addTransferEntry(senderAccount.getAccountNumber(),receiverAccount.getAccountNumber(),amountSent,amountReceived,dbTransactionRequest.getId());

        return dbTransactionRequest;
    }

    void deposit(Account account,BigDecimal amount){
        BigDecimal balance=account.getCurrentBalance();
        balance=balance.add(amount);
        account.setCurrentBalance(balance);
        accountRepository.save(account);
    }
    void deduct(Account account,BigDecimal amount){
        BigDecimal balance=account.getCurrentBalance();
        balance=balance.subtract(amount);
        account.setCurrentBalance(balance);
        accountRepository.save(account);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    public TransactionRequest cancel(Long transactionId) {
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()->new RuntimeException("Transaction not found"));
        if(!transactionRequest.getStatus().equals("PENDING")){
            throw new RuntimeException("only PENDING transactions can be cancelled");
        }
        transactionRequest.setStatus("CANCELLED");
        transactionRequest.setRejectionReason("User Cancelled");
        transactionRequest.setProcessedAt(LocalDateTime.now());
        return transactionRequestRepository.save(transactionRequest);
    }

}
