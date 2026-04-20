package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.user.UserRequest;
import com.vaultpay.vault_pay.dto.user.UserResponse;
import com.vaultpay.vault_pay.entity.User;
import com.vaultpay.vault_pay.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    UserController(UserService userService,PasswordEncoder passwordEncoder){
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
    }

    @PostMapping("/create")
    public ResponseEntity<UserResponse>addStaff(UserRequest userRequest){
        User user=new User();
        user.setUsername(userRequest.username());
        user.setPassword(passwordEncoder.encode(userRequest.password()));
        user.setEmail(userRequest.email());
        user.setRole(userRequest.role());
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>>getAllUsers(){
        List<User>users=userService.findAll();
        return ResponseEntity.ok(users.stream().map(UserResponse::fromEntity).toList());
    }
    @DeleteMapping("/{id}/Delete")
    public ResponseEntity<?>deleteUser(@PathVariable("id")Long userId){
        userService.delete(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
