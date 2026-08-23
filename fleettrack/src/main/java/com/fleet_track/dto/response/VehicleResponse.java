package com.fleet_track.dto.response;

import com.fleet_track.enums.VehicleStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record VehicleResponse(
        UUID id,
        String make,
        String model,
        Integer year,
        String licensePlate,
        String vin,
        VehicleStatus status,
        Integer odometerKm,
        UUID assignedDriverId,
        String assignedDriverName,
        Double lastLatitude,
        Double lastLongitude,
        Instant lastLocationAt,
        Instant createdAt,
        Instant updatedAt
) {}