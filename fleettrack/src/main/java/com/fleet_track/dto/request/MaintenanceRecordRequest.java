package com.fleet_track.dto.request;

import com.fleet_track.enums.MaintenanceType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MaintenanceRecordRequest(
        @NotNull UUID vehicleId,
        @NotNull MaintenanceType maintenanceType,
        @NotNull @PastOrPresent LocalDate serviceDate,
        LocalDate nextDueDate,
        @PositiveOrZero Integer odometerKm,
        @PositiveOrZero BigDecimal cost,
        String notes
) {}