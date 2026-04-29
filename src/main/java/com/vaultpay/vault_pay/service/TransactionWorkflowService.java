package com.vaultpay.vault_pay.service;

import com.vaultpay.vault_pay.dto.transaction.RejectRequest;
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
import java.util.List;

@Service
public class TransactionWorkflowService {
    private final AccountRepository accountRepository;
    private final TransactionRequestRepository transactionRequestRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    @Value("${vaultpay.limits.high-value-threshold}")
    private BigDecimal highValueThreshold;
    TransactionWorkflowService(AccountRepository accountRepository,TransactionRequestRepository transactionRequestRepository,UserRepository userRepository , TransactionService transactionService){
        this.accountRepository=accountRepository;
        this.transactionRequestRepository=transactionRequestRepository;
        this.userRepository=userRepository;
        this.transactionService=transactionService;
    }

    @PreAuthorize("hasRole('CLERK')")
    public TransactionRequest approveByClerk(Long transactionId, String checkerName) {
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
        transactionService.deposit(receiver,transactionRequest.getAmount());
        accountRepository.save(receiver);

        //update maker-checker
        User checker=userRepository.findByUsername(checkerName);
        transactionRequest.setChecker(checker);

        transactionRequestRepository.save(transactionRequest);
        return execute(transactionId);
    }
    private TransactionRequest execute(Long transactionId){
        TransactionRequest transactionRequest=transactionRequestRepository.findById(transactionId).orElseThrow(()->new
                RuntimeException("Transaction not found"));
        if(!transactionRequest.getStatus().equals("APPROVED")){
            throw new RuntimeException("transaction not approved");
        }

        //deposit
        Account receiver=transactionRequest.getReceiver();
        transactionService.deposit(receiver,transactionRequest.getAmount());
        accountRepository.save(receiver);

        //update transaction
        transactionRequest.setStatus("COMPLETED");
        return transactionRequestRepository.save(transactionRequest);
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



    private boolean isHighValue(BigDecimal amount) {
        return amount.compareTo(highValueThreshold)>=0;
    }

}
