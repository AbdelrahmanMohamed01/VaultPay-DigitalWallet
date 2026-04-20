package com.vaultpay.vault_pay.repository;

import com.vaultpay.vault_pay.entity.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry,Long> {
}
