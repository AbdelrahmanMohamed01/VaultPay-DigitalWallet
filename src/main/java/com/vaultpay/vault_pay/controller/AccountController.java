package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.account.AccountRequest;
import com.vaultpay.vault_pay.dto.account.AccountResponse;
import com.vaultpay.vault_pay.dto.account.LockRequest;
import com.vaultpay.vault_pay.entity.Account;
import com.vaultpay.vault_pay.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    AccountController(AccountService accountService){
        this.accountService=accountService;
    }
    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/create")
    public ResponseEntity<AccountResponse>createAccount(@RequestBody AccountRequest accountRequest, Authentication authentication){
        Account account=accountService.createAccount(accountRequest,authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(AccountResponse.fromEntity(account));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping
    public ResponseEntity<List<AccountResponse>>getAccounts(Authentication authentication){
        List<Account>accounts=accountService.findAccountsByUsername(authentication.getName());
        return ResponseEntity.ok(accounts.stream().map(AccountResponse::fromEntity).toList());
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccountByAccountNumber(@PathVariable("accountNumber")String accountNumber){
        Account account=accountService.findAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }
    @PutMapping("/{accountNumber}/lock")
    public ResponseEntity<AccountResponse>lockAccount(@PathVariable("accountNumber")String accountNumber,@RequestBody LockRequest lockRequest,Authentication authentication){
        Account account=accountService.lockAccount(accountNumber,authentication.getName(),lockRequest);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }
    @PutMapping("/{accountNumber}/unlock")
    public ResponseEntity<AccountResponse>unlockAccount(@PathVariable("accountNumber")String accountNumber){
        Account account=accountService.unlockAccount(accountNumber);
        return ResponseEntity.ok(AccountResponse.fromEntity(account));
    }
}
