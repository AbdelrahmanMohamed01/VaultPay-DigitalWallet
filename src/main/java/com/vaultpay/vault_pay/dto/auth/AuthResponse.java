package com.vaultpay.vault_pay.dto.auth;

public record AuthResponse(String jwt,String username,String role,String expireAt) {
}
