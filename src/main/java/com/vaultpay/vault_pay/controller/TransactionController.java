package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.transaction.*;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.service.TransactionQueryService;
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
    private final TransactionQueryService transactionQueryService;
    TransactionController(TransactionService transactionService,TransactionQueryService transactionQueryService){
        this.transactionService=transactionService;
        this.transactionQueryService=transactionQueryService;
    }


    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse>getTransactionById(@PathVariable("id") Long transactionId){
        TransactionRequest transactionRequest=transactionQueryService.findTransactionById(transactionId);
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }


    @GetMapping
    public ResponseEntity<List<TransactionResponse>>getTransactionsByUsername(Authentication authentication){
        List<TransactionRequest> transactionRequestList=transactionQueryService.findTransactionsByUsername(authentication.getName());
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @GetMapping("/account-status")
    public ResponseEntity<List<TransactionResponse >>getTransactionsByAccountNumberAndStatus(@RequestParam("accountNumber")String accountNumber,@RequestParam("status")String status){
        List<TransactionRequest>transactionRequestList=transactionQueryService.findTransactionsByAccountNumberAndStatus(accountNumber,status);
        return ResponseEntity.ok(transactionRequestList.stream().map(TransactionResponse::fromEntity).toList());
    }


    @PostMapping("/deposit")
    public ResponseEntity<DepositResponse>deposit(@RequestBody DepositRequest depositRequest, Authentication authentication, @RequestHeader("Idempotency-Key") String idempotencyKey){
        TransactionRequest transactionRequest=transactionService.processDeposit(depositRequest,authentication.getName(),idempotencyKey);
        return ResponseEntity.ok().header("Idempotency-Key").body(DepositResponse.fromEntity(transactionRequest));
    }




    @PutMapping("/{id}/cancel")
    public ResponseEntity<CancelResponse>cancelTransaction(@PathVariable("id")Long transactionId){
        TransactionRequest transactionRequest=transactionService.cancel(transactionId);
        return ResponseEntity.ok(CancelResponse.fromEntity(transactionRequest));
    }


    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse>transfer(@RequestBody TransferRequest transferRequest,@RequestHeader("Idempotency-Key") String idempotencyKey){
        TransactionRequest transactionRequest=transactionService.processTransfer(transferRequest,idempotencyKey);
        return ResponseEntity.ok().header("Idempotency-Key",idempotencyKey).body(TransferResponse.fromEntity(transactionRequest));
    }


}
