package com.fleet_track.service;

import com.fleet_track.dto.request.LoginRequest;
import com.fleet_track.dto.request.RefreshTokenRequest;
import com.fleet_track.dto.request.RegisterRequest;
import com.fleet_track.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(RefreshTokenRequest request);
}
