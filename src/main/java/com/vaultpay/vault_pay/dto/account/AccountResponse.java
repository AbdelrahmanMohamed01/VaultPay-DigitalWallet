package com.vaultpay.vault_pay.dto.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vaultpay.vault_pay.entity.Account;

import java.math.BigDecimal;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccountResponse(Long id,String accountNumber, String accountName, BigDecimal balance, String currency, String status, String ownerName,String lockBy , String lockReason) {
    public static AccountResponse fromEntity(Account account){
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountName(),
                account.getCurrentBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getUser().getUsername(),
                account.getLocker().getUsername(),
                account.getLockReason()
        );
    }
}
