package com.fleettrack.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record VehicleSummaryResponse(
        UUID id,
        String make,
        String model,
        String licensePlate,
        String status
) {}