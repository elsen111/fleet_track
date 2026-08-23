package com.fleet_track.dto.response;

import com.fleet_track.enums.MaintenanceType;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
public record MaintenanceRecordResponse(
        UUID id,
        UUID vehicleId,
        String vehicleLicensePlate,
        MaintenanceType maintenanceType,
        LocalDate serviceDate,
        LocalDate nextDueDate,
        Integer odometerKm,
        BigDecimal cost,
        String notes
) {}