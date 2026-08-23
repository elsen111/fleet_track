package com.fleet_track.dto.response;

import com.fleet_track.enums.NotificationType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record NotificationMessage(
        NotificationType type,
        UUID vehicleId,
        String licensePlate,
        String message,
        Instant timestamp
) {}