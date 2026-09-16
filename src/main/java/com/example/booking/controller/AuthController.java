package com.example.booking.controller;

import com.example.booking.dto.AuthDtos;
import com.example.booking.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    public AuthController(AuthService auth) { this.auth = auth; }
    @PostMapping("/login") public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest request) { return auth.login(request); }
}
