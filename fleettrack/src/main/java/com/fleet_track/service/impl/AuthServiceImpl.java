package com.fleet_track.service.impl;

import com.fleet_track.dto.request.LoginRequest;
import com.fleet_track.dto.request.RefreshTokenRequest;
import com.fleet_track.dto.request.RegisterRequest;
import com.fleet_track.dto.response.AuthResponse;
import com.fleet_track.entity.UserEntity;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.InvalidTokenException;
import com.fleet_track.repository.UserRepository;
import com.fleet_track.security.CustomUserDetails;
import com.fleet_track.security.JwtService;
import com.fleet_track.security.CustomUserDetailsService;
import com.fleet_track.service.AuthService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("A user with email '" + request.email() + "' already exists");
        }

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(request.role())
                .enabled(true)
                .build();

        userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return AuthResponse.of(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        return AuthResponse.of(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails));
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        try {
            if (!jwtService.isRefreshToken(token)) {
                throw new InvalidTokenException("Provided token is not a refresh token");
            }
            String email = jwtService.extractEmail(token);
            CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, userDetails)) {
                throw new InvalidTokenException("Refresh token is invalid or expired");
            }

            return AuthResponse.of(
                    jwtService.generateAccessToken(userDetails),
                    jwtService.generateRefreshToken(userDetails));
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }
    }
}