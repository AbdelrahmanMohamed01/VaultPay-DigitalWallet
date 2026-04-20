package com.vaultpay.vault_pay.dto.user;

import com.vaultpay.vault_pay.entity.User;

public record UserResponse(Long id, String username, String email, String role) {
    public static UserResponse fromEntity(User user){
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getRole());
    }
}
