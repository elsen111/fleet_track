package com.fleet_track.dto.request;

import com.fleet_track.enums.LicenseType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;

public record DriverUpdateRequest(
        String fullName,
        @Email String email,
        String phone,
        String licenseNumber,
        LicenseType licenseType,
        @Future LocalDate licenseExpiry,
        Boolean active
) {}