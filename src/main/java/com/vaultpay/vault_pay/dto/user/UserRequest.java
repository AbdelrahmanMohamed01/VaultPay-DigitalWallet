package com.vaultpay.vault_pay.dto.user;

public record UserRequest(Long id, String username,String password, String email, String role) {
}
