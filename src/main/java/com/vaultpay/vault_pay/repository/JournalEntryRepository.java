package com.vaultpay.vault_pay.repository;

import com.vaultpay.vault_pay.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry,Long> {
    @Query("select j from JournalEntry j where j.account.accountNumber=:accountNumber order by j.createdAt desc")
    List<JournalEntry> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);

    JournalEntry findByTransactionGroupId(Long transactionId);
}
