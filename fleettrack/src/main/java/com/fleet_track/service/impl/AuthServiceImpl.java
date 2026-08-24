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
import com.fleet_track.security.CustomUserDetailsService;
import com.fleet_track.security.JwtService;
import com.fleet_track.service.AuthService;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
        log.info("Registering new user with email {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration rejected, email already exists: {}", request.email());
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
        log.info("User registered successfully with id {} and role {}", user.getId(), user.getRole());

        CustomUserDetails userDetails = new CustomUserDetails(user);
        return AuthResponse.of(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email {}", request.email());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        CustomUserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        log.info("Login successful for user id {}", userDetails.getId());

        return AuthResponse.of(
                jwtService.generateAccessToken(userDetails),
                jwtService.generateRefreshToken(userDetails));
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();
        log.info("Token refresh requested");

        try {
            if (!jwtService.isRefreshToken(token)) {
                log.warn("Refresh rejected, provided token is not a refresh token");
                throw new InvalidTokenException("Provided token is not a refresh token");
            }

            String email = jwtService.extractEmail(token);
            CustomUserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtService.isTokenValid(token, userDetails)) {
                log.warn("Refresh rejected, token invalid or expired for user {}", email);
                throw new InvalidTokenException("Refresh token is invalid or expired");
            }

            log.info("Token refreshed successfully for user {}", email);
            return AuthResponse.of(
                    jwtService.generateAccessToken(userDetails),
                    jwtService.generateRefreshToken(userDetails));
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Refresh rejected, malformed or expired token: {}", e.getMessage());
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }
    }
}