package com.fleet_track.mapper;

import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.request.VehicleUpdateRequest;
import com.fleet_track.dto.response.VehicleResponse;
import com.fleettrack.dto.response.VehicleSummaryResponse;
import com.fleet_track.entity.VehicleEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDriver", ignore = true)
    @Mapping(target = "lastLatitude", ignore = true)
    @Mapping(target = "lastLongitude", ignore = true)
    @Mapping(target = "lastLocationAt", ignore = true)
    VehicleEntity toEntity(VehicleCreateRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "assignedDriver", ignore = true)
    void updateEntityFromRequest(VehicleUpdateRequest request, @MappingTarget VehicleEntity entity);

    @Mapping(target = "assignedDriverId", source = "assignedDriver.id")
    @Mapping(target = "assignedDriverName", source = "assignedDriver.fullName")
    VehicleResponse toResponse(VehicleEntity entity);

    VehicleSummaryResponse toSummaryResponse(VehicleEntity entity);
}