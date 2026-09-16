package com.example.booking.service;

import com.example.booking.dto.AuthDtos;
import com.example.booking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager; private final JwtService jwt;
    public AuthService(AuthenticationManager authenticationManager, JwtService jwt) { this.authenticationManager = authenticationManager; this.jwt = jwt; }
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        var authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String role = user.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return new AuthDtos.LoginResponse(jwt.generate(user), user.getUsername(), role);
    }
}
