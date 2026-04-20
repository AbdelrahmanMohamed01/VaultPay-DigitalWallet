package com.vaultpay.vault_pay.dto.transaction;

import java.math.BigDecimal;

public record TransferRequest(String senderAccountNumber, String receiverAccountNumber, BigDecimal amount,String currency) {
}
