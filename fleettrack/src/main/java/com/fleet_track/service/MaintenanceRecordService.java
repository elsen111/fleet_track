package com.fleet_track.service;

import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.dto.response.MaintenanceRecordResponse;
import com.fleet_track.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MaintenanceRecordService {
    MaintenanceRecordResponse create(MaintenanceRecordRequest request);
    PagedResponse<MaintenanceRecordResponse> getByVehicle(UUID vehicleId, Pageable pageable);
    void delete(UUID id);
}