package com.vaultpay.vault_pay.repository;

import com.vaultpay.vault_pay.entity.TransactionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface TransactionRequestRepository extends JpaRepository<TransactionRequest,Long> {
    TransactionRequest findByIdempotencyKey(String idempotencyKey);
    @Query("select t from TransactionRequest  t where t.sender.user.username=:username")
    List<TransactionRequest> findBySenderUsername(String username);

    @Query("select t from TransactionRequest  t where t.receiver.user.username=:username")
    List<TransactionRequest> findByReceiverUsername(String username);

    @Query("select t from TransactionRequest  t where t.sender.accountNumber=:accountNumber")
    List<TransactionRequest>findBySenderAccountNumber(String accountNumber);

    @Query("select t from TransactionRequest t where t.receiver.accountNumber=:accountNumber")
    List<TransactionRequest>findByReceiverAccountNumber(String accountNumber);
    @Query("select t from TransactionRequest t where t.receiver.accountNumber=:accountNumber and t.status=:status")
    List<TransactionRequest>findByReceiverAccountNumberAndStatus(String accountNumber,String status);

    @Query("select t from TransactionRequest t where t.sender.accountNumber=:accountNumber and t.status=:status")
    List<TransactionRequest>findBySenderAccountNumberAndStatus(String accountNumber,String status);

    List<TransactionRequest>findByAmountGreaterThanEqual(BigDecimal Ammount);
}
