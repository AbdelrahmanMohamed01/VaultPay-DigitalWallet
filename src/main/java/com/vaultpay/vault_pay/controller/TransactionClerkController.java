package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.transaction.RejectRequest;
import com.vaultpay.vault_pay.dto.transaction.TransactionResponse;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.service.TransactionQueryService;
import com.vaultpay.vault_pay.service.TransactionService;
import com.vaultpay.vault_pay.service.TransactionWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clerk/transactions/")
public class TransactionClerkController {
    TransactionService transactionService;
    TransactionQueryService transactionQueryService;
    TransactionWorkflowService transactionWorkflowService;
    TransactionClerkController(TransactionService transactionService,TransactionWorkflowService transactionWorkflowService,TransactionQueryService transactionQueryService){
        this.transactionService=transactionService;
        this.transactionWorkflowService=transactionWorkflowService;
    }

    @PreAuthorize("hasRole('CLERK')")
    @GetMapping("/user")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByUsername(@RequestParam("username") String username){
        List<TransactionRequest> transactionRequestList=transactionQueryService.findTransactionsByUsername(username);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }


    @GetMapping("/account")
    public ResponseEntity<List<TransactionResponse>>getTransactionsByAccountNumber(@RequestParam("accountNumber") String accountNumber){
        List<TransactionRequest> transactionRequestList=transactionQueryService.findTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TransactionResponse> approveTransactionByClerk(@PathVariable("id")Long transactionId, Authentication authentication){
        TransactionRequest transactionRequest=transactionWorkflowService.approveByClerk(transactionId,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransactionResponse>rejectTransactionByClerk(@PathVariable("id")Long transactionId, RejectRequest rejectRequest, Authentication authentication){
        TransactionRequest transactionRequest=transactionWorkflowService.rejectByClerk(transactionId,rejectRequest,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

}
