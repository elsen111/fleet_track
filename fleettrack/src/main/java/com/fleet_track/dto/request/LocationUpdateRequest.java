package com.fleet_track.dto.request;

import jakarta.validation.constraints.NotNull;

public record LocationUpdateRequest(
        @NotNull String vehicleId,
        @NotNull Double latitude,
        @NotNull Double longitude
) {}