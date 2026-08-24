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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaintenanceRecordServiceImpl implements MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRecordMapper maintenanceRecordMapper;

    @Override
    @Transactional
    public MaintenanceRecordResponse create(MaintenanceRecordRequest request) {
        log.info("Creating maintenance record for vehicle {}", request.vehicleId());

        VehicleEntity vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> {
                    log.warn("Maintenance record creation rejected, vehicle not found with id {}", request.vehicleId());
                    return new ResourceNotFoundException("Vehicle not found with id: " + request.vehicleId());
                });

        MaintenanceRecordEntity record = maintenanceRecordMapper.toEntity(request);
        record.setVehicle(vehicle);
        maintenanceRecordRepository.save(record);
        log.info("Maintenance record {} created for vehicle {}", record.getId(), vehicle.getId());

        if (vehicle.getStatus() == VehicleStatus.ACTIVE) {
            vehicle.setStatus(VehicleStatus.IN_MAINTENANCE);
            vehicleRepository.save(vehicle);
            log.info("Vehicle {} status changed to IN_MAINTENANCE", vehicle.getId());
        }

        return maintenanceRecordMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MaintenanceRecordResponse> getByVehicle(UUID vehicleId, Pageable pageable) {
        log.debug("Fetching maintenance records for vehicle {}", vehicleId);

        if (!vehicleRepository.existsById(vehicleId)) {
            log.warn("Maintenance record lookup rejected, vehicle not found with id {}", vehicleId);
            throw new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
        }

        Page<MaintenanceRecordEntity> page = maintenanceRecordRepository.findByVehicleId(vehicleId, pageable);
        log.debug("Found {} maintenance records for vehicle {}", page.getTotalElements(), vehicleId);

        return PagedResponse.from(page.map(maintenanceRecordMapper::toResponse));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting maintenance record with id {}", id);

        MaintenanceRecordEntity record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Maintenance record not found with id {}", id);
                    return new ResourceNotFoundException("Maintenance record not found with id: " + id);
                });

        maintenanceRecordRepository.delete(record);
        log.info("Maintenance record {} deleted successfully", id);
    }
}