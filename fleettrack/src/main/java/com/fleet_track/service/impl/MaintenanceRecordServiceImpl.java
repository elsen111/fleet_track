package com.fleet_track.service.impl;

import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.dto.response.MaintenanceRecordResponse;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.entity.MaintenanceRecordEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.MaintenanceRecordMapper;
import com.fleet_track.repository.MaintenanceRecordRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.MaintenanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    @Override
    @Transactional
    public MaintenanceRecordResponse create(MaintenanceRecordRequest request) {
        VehicleEntity vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.vehicleId()));

        MaintenanceRecordEntity record = maintenanceRecordMapper.toEntity(request);
        record.setVehicle(vehicle);
        maintenanceRecordRepository.save(record);

        // Service just happened — a vehicle mid-service is naturally flagged as in maintenance.
        if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
            vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
            vehicleRepository.save(vehicle);
        }

        return maintenanceRecordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceRecordResponse> getByVehicle(UUID vehicleId, Pageable pageable) {
        if (!vehicleRepository.existsById(vehicleId)) {
            throw new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
        }
        Page<MaintenanceRecordEntity> page = maintenanceRecordRepository.findByVehicleId(vehicleId, pageable);
        return PagedResponse.from(page.map(maintenanceRecordMapper::toResponse));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        MaintenanceRecordEntity record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance record not found with id: " + id));
        maintenanceRecordRepository.delete(record);
    }
}