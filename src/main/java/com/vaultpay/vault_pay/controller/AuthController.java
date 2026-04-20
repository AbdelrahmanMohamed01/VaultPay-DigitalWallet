package com.vaultpay.vault_pay.controller;

import com.vaultpay.vault_pay.dto.auth.AuthResponse;
import com.vaultpay.vault_pay.dto.auth.LoginRequest;
import com.vaultpay.vault_pay.dto.auth.RegisterRequest;
import com.vaultpay.vault_pay.dto.user.UserResponse;
import com.vaultpay.vault_pay.entity.User;
import com.vaultpay.vault_pay.security.JwtService;
import com.vaultpay.vault_pay.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    AuthController(UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService){
        this.userService=userService;
        this.passwordEncoder=passwordEncoder;
        this.authenticationManager=authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest){
        if(!registerRequest.role().equals("ROLE_CUSTOMER")){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("cannot register with this role.");
        }
        User user=new User();
        user.setUsername(registerRequest.username());
        user.setPassword(passwordEncoder.encode(registerRequest.password()));
        user.setEmail(registerRequest.email());
        user.setRole(registerRequest.role());
        userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?>login(@RequestBody LoginRequest loginRequest){
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.username(),loginRequest.password()));
        }
        catch (AuthenticationException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user=userService.findByUsername(loginRequest.username());
        String jwt=jwtService.generateToken(user.getUsername(),user.getRole());
        return ResponseEntity.ok(new AuthResponse(jwt,user.getUsername(),user.getRole(),"1 Hour"));
    }
}
