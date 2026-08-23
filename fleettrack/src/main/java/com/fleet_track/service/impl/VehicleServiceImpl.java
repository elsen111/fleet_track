package com.fleet_track.service.impl;

import com.fleet_track.config.RedisConfig;
import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.request.VehicleUpdateRequest;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.dto.response.VehicleResponse;
import com.fleet_track.entity.DriverEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.exception.BusinessRuleException;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.VehicleMapper;
import com.fleet_track.repository.DriverRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.VehicleService;
import com.fleet_track.repository.specification.VehicleSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final VehicleMapper vehicleMapper;

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.VEHICLE_SUMMARY_CACHE, allEntries = true)
    public VehicleResponse create(VehicleCreateRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new DuplicateResourceException(
                    "A vehicle with license plate '" + request.licensePlate() + "' already exists");
        }
        if (request.vin() != null && vehicleRepository.existsByVin(request.vin())) {
            throw new DuplicateResourceException("A vehicle with VIN '" + request.vin() + "' already exists");
        }

        VehicleEntity vehicle = vehicleMapper.toEntity(request);
        vehicle.setStatus(request.status() != null ? request.status() : VehicleStatus.ACTIVE);
        vehicle.setOdometerKm(request.odometerKm() != null ? request.odometerKm() : 0);

        if (request.assignedDriverId() != null) {
            vehicle.setAssignedDriver(resolveActiveDriver(request.assignedDriverId()));
        }

        vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(UUID id) {
        return vehicleMapper.toResponse(findVehicleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VehicleResponse> search(VehicleStatus status, Integer year, UUID driverId,
                                                 String search, Pageable pageable) {
        Page<VehicleEntity> page = vehicleRepository.findAll(
                VehicleSpecification.withFilters(status, year, driverId, search), pageable);
        return PagedResponse.from(page.map(vehicleMapper::toResponse));
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.VEHICLE_SUMMARY_CACHE, allEntries = true)
    public VehicleResponse update(UUID id, VehicleUpdateRequest request) {
        VehicleEntity vehicle = findVehicleOrThrow(id);

        if (request.licensePlate() != null && !request.licensePlate().equals(vehicle.getLicensePlate())
                && vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new DuplicateResourceException(
                    "A vehicle with license plate '" + request.licensePlate() + "' already exists");
        }

        vehicleMapper.updateEntityFromRequest(request, vehicle);

        if (request.assignedDriverId() != null) {
            vehicle.setAssignedDriver(resolveActiveDriver(request.assignedDriverId()));
        }

        vehicleRepository.save(vehicle);
        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.VEHICLE_SUMMARY_CACHE, allEntries = true)
    public void delete(UUID id) {
        VehicleEntity vehicle = findVehicleOrThrow(id);
        vehicleRepository.delete(vehicle);
    }

    @Override
    @Transactional
    public void updateLocation(UUID vehicleId, double latitude, double longitude) {
        VehicleEntity vehicle = findVehicleOrThrow(vehicleId);
        vehicle.setLastLatitude(latitude);
        vehicle.setLastLongitude(longitude);
        vehicle.setLastLocationAt(Instant.now());
        vehicleRepository.save(vehicle);
    }

    @Cacheable(value = RedisConfig.VEHICLE_SUMMARY_CACHE, key = "'all'")
    public java.util.List<com.fleettrack.dto.response.VehicleSummaryResponse> getAllSummariesCached() {
        return vehicleRepository.findAll().stream().map(vehicleMapper::toSummaryResponse).toList();
    }

    private VehicleEntity findVehicleOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private DriverEntity resolveActiveDriver(UUID driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));
        if (!driver.isActive()) {
            throw new BusinessRuleException("Cannot assign an inactive driver to a vehicle");
        }
        return driver;
    }
}