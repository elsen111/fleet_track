package com.fleet_track.service;

import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.request.VehicleUpdateRequest;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.dto.response.VehicleResponse;
import com.fleet_track.enums.VehicleStatus;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VehicleService {
    VehicleResponse create(VehicleCreateRequest request);
    VehicleResponse getById(UUID id);
    PagedResponse<VehicleResponse> search(VehicleStatus status, Integer year, UUID driverId,
                                          String search, Pageable pageable);
    VehicleResponse update(UUID id, VehicleUpdateRequest request);
    void delete(UUID id);
    void updateLocation(UUID vehicleId, double latitude, double longitude);
}