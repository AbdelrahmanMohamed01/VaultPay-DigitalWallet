package com.vaultpay.vault_pay.repository;

import com.vaultpay.vault_pay.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {
    public Account findByAccountNumber(String accountNumber);
}
