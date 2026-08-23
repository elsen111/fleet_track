package com.fleet_track.dto.request;

import com.fleet_track.enums.LicenseType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record DriverCreateRequest(
        @NotBlank
        String fullName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phone,

        @NotBlank
        String licenseNumber,

        @NotNull
        LicenseType licenseType,

        @NotNull
        @Future
        LocalDate licenseExpiry
) {}