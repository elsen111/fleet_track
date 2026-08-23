package com.fleet_track.mapper;

import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.dto.response.MaintenanceRecordResponse;
import com.fleet_track.entity.MaintenanceRecordEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaintenanceRecordMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vehicle", ignore = true)
    MaintenanceRecordEntity toEntity(MaintenanceRecordRequest request);

    @Mapping(target = "vehicleId", source = "vehicle.id")
    @Mapping(target = "vehicleLicensePlate", source = "vehicle.licensePlate")
    MaintenanceRecordResponse toResponse(MaintenanceRecordEntity entity);
}