package com.fleet_track.service;

import com.fleet_track.dto.request.LoginRequest;
import com.fleet_track.dto.request.RefreshTokenRequest;
import com.fleet_track.dto.request.RegisterRequest;
import com.fleet_track.dto.response.AuthResponse;
import com.fleet_track.entity.UserEntity;
import com.fleet_track.enums.Role;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.InvalidTokenException;
import com.fleet_track.repository.UserRepository;
import com.fleet_track.security.CustomUserDetails;
import com.fleet_track.security.CustomUserDetailsService;
import com.fleet_track.security.JwtService;
import com.fleet_track.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private CustomUserDetailsService userDetailsService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(userRepository, passwordEncoder, authenticationManager,
                jwtService, userDetailsService);
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("taken@fleettrack.com", "pass1234", "Elshan", Role.ADMIN);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("taken@fleettrack.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_createsUserAndReturnsTokens_whenEmailIsAvailable() {
        RegisterRequest request = new RegisterRequest("new@fleettrack.com", "pass1234", "Elshan", Role.FLEET_MANAGER);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed");
        when(jwtService.generateAccessToken(any(CustomUserDetails.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(CustomUserDetails.class))).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void login_authenticatesAndReturnsTokens_whenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("user@fleettrack.com", "pass1234");
        UserEntity user = UserEntity.builder()
                .email(request.email()).password("hashed").fullName("Elshan")
                .role(Role.ADMIN).enabled(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(userDetailsService.loadUserByUsername(request.email())).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void refresh_throwsInvalidTokenException_whenTokenIsNotARefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest("some-access-token");
        when(jwtService.isRefreshToken("some-access-token")).thenReturn(false);

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_returnsNewTokens_whenRefreshTokenIsValid() {
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");
        UserEntity user = UserEntity.builder()
                .email("user@fleettrack.com").password("hashed").fullName("Elshan")
                .role(Role.ADMIN).enabled(true).build();
        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.isRefreshToken("valid-refresh-token")).thenReturn(true);
        when(jwtService.extractEmail("valid-refresh-token")).thenReturn("user@fleettrack.com");
        when(userDetailsService.loadUserByUsername("user@fleettrack.com")).thenReturn(userDetails);
        when(jwtService.isTokenValid("valid-refresh-token", userDetails)).thenReturn(true);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(userDetails)).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh(request);

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isEqualTo("new-refresh-token");
    }
}