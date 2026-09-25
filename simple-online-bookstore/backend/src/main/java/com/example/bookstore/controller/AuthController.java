package com.example.bookstore.controller;

import com.example.bookstore.dto.Requests.*;
import com.example.bookstore.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService authService) {
        this.auth = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody Credentials credentials) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(auth.register(credentials));
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody Credentials credentials) {
        return auth.login(credentials);
    }
}
