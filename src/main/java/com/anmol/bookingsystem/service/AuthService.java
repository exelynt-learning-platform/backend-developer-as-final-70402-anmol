package com.anmol.bookingsystem.service;

import com.anmol.bookingsystem.dto.LoginRequest;
import com.anmol.bookingsystem.dto.LoginResponse;
import com.anmol.bookingsystem.entity.User;
import com.anmol.bookingsystem.repository.UserRepository;
import com.anmol.bookingsystem.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // AuthenticationManager handles both "user not found" and "bad password"
        // as BadCredentialsException → 401. No user existence info leaks.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        String token = jwtUtil.generateToken(userDetails);
        return new LoginResponse(token, user.getRole().name());
    }
}