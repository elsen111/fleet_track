package com.fleet_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet_track.dto.request.LoginRequest;
import com.fleet_track.dto.request.RegisterRequest;
import com.fleet_track.dto.response.AuthResponse;
import com.fleet_track.enums.Role;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;

    @Test
    void register_returns201AndTokens_whenRequestIsValid() throws Exception {
        RegisterRequest request = new RegisterRequest("elshan@fleettrack.com", "pass1234", "Elshan", Role.ADMIN);
        AuthResponse response = AuthResponse.of("access-token", "refresh-token");

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void register_returns400_whenEmailIsInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("not-an-email", "pass1234", "Elshan", Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        RegisterRequest request = new RegisterRequest("elshan@fleettrack.com", "pass1234", "Elshan", Role.ADMIN);

        when(authService.register(any())).thenThrow(
                new DuplicateResourceException("A user with email 'elshan@fleettrack.com' already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200AndTokens_whenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("elshan@fleettrack.com", "pass1234");
        AuthResponse response = AuthResponse.of("access-token", "refresh-token");

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("access-token"));
    }

    @Test
    void login_returns401_whenCredentialsAreInvalid() throws Exception {
        LoginRequest request = new LoginRequest("elshan@fleettrack.com", "wrongpass");

        when(authService.login(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}