package com.fleet_track.mapper;

import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.request.DriverUpdateRequest;
import com.fleet_track.dto.response.DriverResponse;
import com.fleet_track.entity.DriverEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DriverMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    DriverEntity toEntity(DriverCreateRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromRequest(DriverUpdateRequest request, @MappingTarget DriverEntity entity);

    @Mapping(target = "assignedVehicleId", ignore = true)
    @Mapping(target = "assignedVehiclePlate", ignore = true)
    DriverResponse toResponse(DriverEntity entity);
}