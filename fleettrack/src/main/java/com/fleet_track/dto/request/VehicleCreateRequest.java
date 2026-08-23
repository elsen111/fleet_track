package com.fleet_track.dto.request;

import com.fleet_track.enums.VehicleStatus;
import jakarta.validation.constraints.*;

import java.util.UUID;

public record VehicleCreateRequest(
        @NotBlank
        String make,

        @NotBlank
        String model,

        @NotNull
        @Min(1980)
        @Max(2100)
        Integer year,

        @NotBlank
        String licensePlate,

        String vin,

        VehicleStatus status,

        @PositiveOrZero
        Integer odometerKm,

        UUID assignedDriverId
) {}