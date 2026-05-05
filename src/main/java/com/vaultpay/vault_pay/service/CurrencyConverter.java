package com.vaultpay.vault_pay.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
@Service
public class CurrencyConverter {
    private final Map<String, BigDecimal>rates=Map.of(
            "USD",BigDecimal.ONE,
            "EGP",new BigDecimal("48.50"),
            "EUR",new BigDecimal("0.92")
            );
    public BigDecimal covert(BigDecimal amount,String from,String to){
        if(from.equals(to))
            return amount;
        // Fix: Multiply first, then call setScale on the resulting BigDecimal
        BigDecimal amountInBase = amount.divide(rates.get(from), 4, BigDecimal.ROUND_HALF_UP);

        return amountInBase.multiply(rates.get(to))
                .setScale(4, BigDecimal.ROUND_HALF_UP);
    }
}
