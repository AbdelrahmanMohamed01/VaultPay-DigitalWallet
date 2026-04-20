package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.transaction.*;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
    private final TransactionService transactionService;
    TransactionController(TransactionService transactionService){
        this.transactionService=transactionService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse>getTransactionById(@PathVariable("id") Long transactionId){
        TransactionRequest transactionRequest=transactionService.findTransactionById(transactionId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>>getTransactionsByUsername(Authentication authentication){
        List<TransactionRequest> transactionRequestList=transactionService.findTransactionsByUsername(authentication.getName());
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PreAuthorize("hasRole('CLERK')")
    @GetMapping("/user")
    public ResponseEntity<List<TransactionResponse>>getTransactionsByUsername(@RequestParam("username") String username){
        List<TransactionRequest> transactionRequestList=transactionService.findTransactionsByUsername(username);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/account")
    public ResponseEntity<List<TransactionResponse>>getTransactionsByAccountNumber(@RequestParam("accountNumber") String accountNumber){
        List<TransactionRequest> transactionRequestList=transactionService.findTransactionsByAccountNumber(accountNumber);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/account-status")
    public ResponseEntity<List<TransactionResponse >>getTransactionsByAccountNumberAndStatus(@RequestParam("accountNumber")String accountNumber,@RequestParam("status")String status){
        List<TransactionRequest>transactionRequestList=transactionService.findTransactionsByAccountNumberAndStatus(accountNumber,status);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse>deposit(@RequestBody DepositRequest depositRequest, Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey){
        TransactionRequest transactionRequest=transactionService.processDeposit(depositRequest,authentication.getName(),idempotencyKey);
        return ResponseEntity.ok().header("Idempotency-Key").body(DepositResponse.fromEntity(transactionRequest));
    }

    @PutMapping("/{id}/approve-clerk")
    public ResponseEntity<TransactionResponse> approveTransactionByClerk(@PathVariable("id")Long transactionId, Authentication authentication){
        TransactionRequest transactionRequest=transactionService.approveByClerk(transactionId,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PutMapping("/{id}/reject-clerk")
    public ResponseEntity<TransactionResponse>rejectTransactionByClerk(@PathVariable("id")Long transactionId, RejectRequest rejectRequest, Authentication authentication){
        TransactionRequest transactionRequest=transactionService.rejectByClerk(transactionId,rejectRequest,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<TransactionResponse>> getHighValueTransactions(){
        List<TransactionRequest>transactionResponseList= transactionService.getHighValueTransactions();
        return ResponseEntity.ok(transactionResponseList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PutMapping("/{id}/approve-manager")
    public ResponseEntity<TransactionResponse> approveTransactionByManager(@PathVariable("id") Long id, Authentication authentication){
        TransactionRequest transactionRequest=transactionService.approveByManager(id,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PutMapping("/{id}/reject-manger")
    public ResponseEntity<TransactionResponse> rejectTransactionByManager(@PathVariable("id") Long transactionId,@RequestBody RejectRequest rejectRequest, Authentication authentication){
        TransactionRequest transactionRequest=transactionService.rejectByManager(transactionId,rejectRequest,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PutMapping("/{id}/cancel")
    public ResponseEntity<CancelResponse>cancelTransaction(@PathVariable("id")Long transactionId){
        TransactionRequest transactionRequest=transactionService.cancel(transactionId);
        return ResponseEntity.ok(CancelResponse.fromEntity(transactionRequest));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse>transfer(@RequestBody TransferRequest transferRequest,@RequestHeader("Idempotency-Key") String idempotencyKey){
        TransactionRequest transactionRequest=transactionService.processTransfer(transferRequest,idempotencyKey);
        return ResponseEntity.ok().header("Idempotency-Key",idempotencyKey).body(TransferResponse.fromEntity(transactionRequest));
    }


}
