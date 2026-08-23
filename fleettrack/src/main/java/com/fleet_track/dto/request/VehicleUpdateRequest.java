package com.fleet_track.dto.request;

import com.fleet_track.enums.LicenseType;
import com.fleet_track.enums.VehicleStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.UUID;

public record VehicleUpdateRequest(
        String make,
        String model,

        @Min(1980)
        @Max(2100)
        Integer year,

        String licensePlate,
        String vin,
        VehicleStatus status,
        @PositiveOrZero
        Integer odometerKm,
        UUID assignedDriverId
) {
    public static record DriverCreateResponse(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank String phone,
            @NotBlank String licenseNumber,
            @NotNull LicenseType licenseType,
            @NotNull @Future LocalDate licenseExpiry
    ) {}
}