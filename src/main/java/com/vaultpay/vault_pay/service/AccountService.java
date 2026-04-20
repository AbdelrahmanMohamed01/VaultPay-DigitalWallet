package com.vaultpay.vault_pay.service;

import com.vaultpay.vault_pay.dto.account.AccountRequest;
import com.vaultpay.vault_pay.dto.account.LockRequest;
import com.vaultpay.vault_pay.entity.Account;
import com.vaultpay.vault_pay.entity.User;
import com.vaultpay.vault_pay.repository.AccountRepository;
import com.vaultpay.vault_pay.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    AccountService(AccountRepository accountRepository,UserRepository userRepository){
        this.accountRepository=accountRepository;
        this.userRepository=userRepository;
    }

    public Account createAccount(AccountRequest accountRequest,String username){
        User user=userRepository.findByUsername(username);
        Account account=new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setAccountName(accountRequest.accountName());
        account.setCurrency(accountRequest.currency());
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        account.setUser(user);
        return accountRepository.save(account);
    }
    private String generateAccountNumber() {
        return ("VP"+ (long)(Math.random()*10000000000L));
    }

    public List<Account> findAccountsByUsername(String username) {
        return userRepository.findByUsername(username).getAccounts();
    }


    public Account findAccountByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
    @PreAuthorize("hasRole('MANAGER')")
    public Account lockAccount(String accountNumber,String lockerName ,LockRequest lockRequest) {
        Account account=accountRepository.findByAccountNumber(accountNumber);
        if(!account.getStatus().equals("ACTIVE")){
            throw new RuntimeException("Cannot lock frozen account");
        }
        User lockerUser=userRepository.findByUsername(lockerName);
        account.setLockReason(lockRequest.lockReason());
        account.setLocker(lockerUser);
        account.setStatus("LOCKED");
        return accountRepository.save(account);
    }
    @PreAuthorize("hasRole('MANAGER')")
    public Account unlockAccount(String accountNumber) {
        Account account=accountRepository.findByAccountNumber(accountNumber);
        if(!account.getStatus().equals("LOCKED")){
            throw new RuntimeException("Cannot lock active account");
        }
        account.setLockReason(null);
        account.setLocker(null);
        account.setStatus("ACTIVE");
        return accountRepository.save(account);
    }
}
