package com.fleet_track.dto.response;

import com.fleet_track.enums.LicenseType;
import lombok.Builder;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record DriverResponse(
        UUID id,
        String fullName,
        String email,
        String phone,
        String licenseNumber,
        LicenseType licenseType,
        LocalDate licenseExpiry,
        boolean active,
        UUID assignedVehicleId,
        String assignedVehiclePlate
) {}