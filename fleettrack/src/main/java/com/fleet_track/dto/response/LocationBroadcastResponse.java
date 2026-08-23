package com.fleet_track.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record LocationBroadcastResponse(
        UUID vehicleId,
        String licensePlate,
        Double latitude,
        Double longitude,
        Instant recordedAt
) {}