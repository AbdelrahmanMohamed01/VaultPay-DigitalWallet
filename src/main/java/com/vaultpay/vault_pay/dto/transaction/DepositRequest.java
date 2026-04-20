package com.vaultpay.vault_pay.dto.transaction;

import java.math.BigDecimal;

public record DepositRequest(String accountNumber, BigDecimal amount,String currency) {
}
