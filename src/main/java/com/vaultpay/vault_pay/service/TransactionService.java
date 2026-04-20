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
    @Value("${vaultpay.limits.high-value-threshold}")
    private BigDecimal highValueThreshold;
    TransactionService(AccountRepository accountRepository,TransactionRequestRepository transactionRequestRepository,UserRepository userRepository){
        this.accountRepository=accountRepository;
        this.transactionRequestRepository=transactionRequestRepository;
        this.userRepository=userRepository;
    }

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
        return transactionRequestRepository.save(transactionRequest);
    }
    public TransactionRequest processTransfer(TransferRequest transferRequest, String idempotencyKey){
        TransactionRequest existingTransactionRequest=transactionRequestRepository.findByIdempotencyKey(idempotencyKey);
        if(existingTransactionRequest!=null){
            return existingTransactionRequest;
        }

        Account senderAccount=accountRepository.findByAccountNumber(transferRequest.senderAccountNumber());
        Account receiverAccount=accountRepository.findByAccountNumber(transferRequest.receiverAccountNumber());

        if(!receiverAccount.getStatus().equals("ACTIVE")){
            throw new RuntimeException("cannot send money to a locked account");
        }

        BigDecimal amount=transferRequest.amount();
        deposit(receiverAccount,amount);
        deduct(senderAccount,amount);

        TransactionRequest transactionRequest=new TransactionRequest();
        transactionRequest.setAmount(amount);
        transactionRequest.setCurrency(transferRequest.currency());
        transactionRequest.setCreatedAt(LocalDateTime.now());
        transactionRequest.setSender(senderAccount);
        transactionRequest.setReceiver(receiverAccount);
        transactionRequest.setIdempotencyKey(idempotencyKey);
        return transactionRequestRepository.save(transactionRequest);
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

    @PreAuthorize("hasRole('MANAGER')")
    public List<TransactionRequest> getHighValueTransactions() {
        return transactionRequestRepository.findByAmountGreaterThanEqual(highValueThreshold);
    }
    private TransactionRequest execute(Long transactionId){
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()->new
                RuntimeException("Transaction not found"));
        if(!transactionRequest.getStatus().equals("APPROVED")){
            throw new RuntimeException("transaction not approved");
        }
        
        //deposit
        Account receiver=transactionRequest.getReceiver();
        deposit(receiver,transactionRequest.getAmount());
        accountRepository.save(receiver);

        //update transaction
        transactionRequest.setStatus("COMPLETED");
        return transactionRequestRepository.save(transactionRequest);
    }

    private boolean isHighValue(BigDecimal amount) {
        return amount.compareTo(highValueThreshold)>=0;
    }
    @PreAuthorize("hasRole('CLERK')")
    public TransactionRequest approveByClerk(Long transactionId,String checkerName) {
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()-> new RuntimeException("Transaction not found"));
        if(!transactionRequest.getStatus().equals("PENDING")){
            throw new RuntimeException("Invalid state for clerk approval");
        }
        //update data
        transactionRequest.setProcessedAt(LocalDateTime.now());

        //update maker-checker
        User checker=userRepository.findByUsername(checkerName);
        transactionRequest.setChecker(checker);

        if(!isHighValue(transactionRequest.getAmount())){
            transactionRequest.setStatus("CLERK_APPROVED");
            return transactionRequestRepository.save(transactionRequest);
        }
        else{
            transactionRequest.setStatus("APPROVED");
            transactionRequestRepository.save(transactionRequest);
            return execute(transactionId);
        }
    }
    @PreAuthorize("hasRole('MANAGER')")
    public TransactionRequest approveByManager(Long transactionId,String checkerName){
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()-> new RuntimeException("Transaction not found"));
        if(!transactionRequest.getStatus().equals("CLERK_APPROVED")){
            throw new RuntimeException("invalid state of manager approval");
        }
        transactionRequest.setStatus("APPROVED");
        //execute
        Account receiver=transactionRequest.getReceiver();
        deposit(receiver,transactionRequest.getAmount());
        accountRepository.save(receiver);

        //update maker-checker
        User checker=userRepository.findByUsername(checkerName);
        transactionRequest.setChecker(checker);

        transactionRequestRepository.save(transactionRequest);
        return execute(transactionId);
    }
    @PreAuthorize("hasRole('CLERK')")
    public TransactionRequest rejectByClerk(Long transactionId, RejectRequest rejectRequest, String checkerName) {
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()-> new RuntimeException("Transaction not found"));

        if(!transactionRequest.getStatus().equals("PENDING")){
            throw new RuntimeException("Only pending transactions can be rejected by clerk");
        }

        User checker=userRepository.findByUsername(checkerName);
        transactionRequest.setChecker(checker);
        transactionRequest.setStatus("Rejected");
        transactionRequest.setRejectionReason(rejectRequest.rejectionReason());
        transactionRequest.setProcessedAt(LocalDateTime.now());

        return transactionRequestRepository.save(transactionRequest);
    }

    @PreAuthorize("hasRole('MANAGER')")
    public TransactionRequest rejectByManager(Long transactionId, RejectRequest rejectRequest, String checkerName) {
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()-> new RuntimeException("Transaction not found"));

        if(!transactionRequest.getStatus().equals("CLERK_APPROVED")){
            throw new RuntimeException("Only clerk-approved transactions can be rejected by manager");
        }

        User checker=userRepository.findByUsername(checkerName);
        transactionRequest.setChecker(checker);
        transactionRequest.setStatus("Rejected");
        transactionRequest.setRejectionReason(rejectRequest.rejectionReason());
        transactionRequest.setProcessedAt(LocalDateTime.now());

        return transactionRequestRepository.save(transactionRequest);
    }

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

    public TransactionRequest findTransactionById(Long transactionId) {
        return transactionRequestRepository.findById(transactionId).orElse(null);
    }

    public List<TransactionRequest> findTransactionsByUsername(String username) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverUsername(username);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderUsername(username);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }

    public List<TransactionRequest> findTransactionsByAccountNumber(String accountNumber) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverAccountNumber(accountNumber);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderAccountNumber(accountNumber);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }

    public List<TransactionRequest> findTransactionsByAccountNumberAndStatus(String accountNumber, String status) {
        List<TransactionRequest>receiverTransactions=transactionRequestRepository.findByReceiverAccountNumberAndStatus(accountNumber,status);
        List<TransactionRequest>senderTransactions=transactionRequestRepository.findBySenderAccountNumberAndStatus(accountNumber,status);
        List<TransactionRequest>result=new ArrayList<>();
        result.addAll(receiverTransactions);
        result.addAll(senderTransactions);
        return result;
    }

}
