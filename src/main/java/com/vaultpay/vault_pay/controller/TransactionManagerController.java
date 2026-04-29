package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.transaction.RejectRequest;
import com.vaultpay.vault_pay.dto.transaction.TransactionResponse;
import com.vaultpay.vault_pay.entity.TransactionRequest;
import com.vaultpay.vault_pay.service.TransactionQueryService;
import com.vaultpay.vault_pay.service.TransactionService;
import com.vaultpay.vault_pay.service.TransactionWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/transactions")
public class TransactionManagerController {
    TransactionService transactionService;
    private final TransactionQueryService transactionQueryService;
    private final TransactionWorkflowService transactionWorkflowService;
    public TransactionManagerController(TransactionService transactionService,TransactionQueryService transactionQueryService,TransactionWorkflowService transactionWorkflowService){
        this.transactionService=transactionService;
        this.transactionQueryService=transactionQueryService;
        this.transactionWorkflowService=transactionWorkflowService;
    }

    @GetMapping("/high-value")
    public ResponseEntity<List<TransactionResponse>> getHighValueTransactions(){
        List<TransactionRequest>transactionResponseList= transactionQueryService.getHighValueTransactions();
        return ResponseEntity.ok(transactionResponseList.stream().map(TransactionResponse::fromEntity).toList());
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<TransactionResponse> approveTransactionByManager(@PathVariable("id") Long id, Authentication authentication){
        TransactionRequest transactionRequest=transactionWorkflowService.approveByManager(id,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<TransactionResponse> rejectTransactionByManager(@PathVariable("id") Long transactionId, @RequestBody RejectRequest rejectRequest, Authentication authentication){
        TransactionRequest transactionRequest=transactionWorkflowService.rejectByManager(transactionId,rejectRequest,authentication.getName());
        return ResponseEntity.ok(TransactionResponse.fromEntity(transactionRequest));
    }

}
