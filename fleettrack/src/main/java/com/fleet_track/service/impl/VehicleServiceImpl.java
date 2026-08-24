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
import com.fleet_track.repository.specification.VehicleSpecification;
import com.fleet_track.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
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
        log.info("Creating vehicle with license plate {}", request.licensePlate());

        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            log.warn("Vehicle creation rejected, duplicate license plate {}", request.licensePlate());
            throw new DuplicateResourceException(
                    "A vehicle with license plate '" + request.licensePlate() + "' already exists");
        }
        if (request.vin() != null && vehicleRepository.existsByVin(request.vin())) {
            log.warn("Vehicle creation rejected, duplicate VIN {}", request.vin());
            throw new DuplicateResourceException("A vehicle with VIN '" + request.vin() + "' already exists");
        }

        VehicleEntity vehicle = vehicleMapper.toEntity(request);
        vehicle.setStatus(request.status() != null ? request.status() : VehicleStatus.ACTIVE);
        vehicle.setOdometerKm(request.odometerKm() != null ? request.odometerKm() : 0);

        if (request.assignedDriverId() != null) {
            vehicle.setAssignedDriver(resolveActiveDriver(request.assignedDriverId()));
        }

        vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id {}", vehicle.getId());

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional(readOnly = true)
    public VehicleResponse getById(UUID id) {
        log.debug("Fetching vehicle with id {}", id);
        return vehicleMapper.toResponse(findVehicleOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<VehicleResponse> search(VehicleStatus status, Integer year, UUID driverId,
                                                 String search, Pageable pageable) {
        log.debug("Searching vehicles with status={}, year={}, driverId={}, search={}, page={}",
                status, year, driverId, search, pageable);

        Page<VehicleEntity> page = vehicleRepository.findAll(
                VehicleSpecification.withFilters(status, year, driverId, search), pageable);

        log.debug("Vehicle search returned {} results out of {}", page.getNumberOfElements(), page.getTotalElements());
        return PagedResponse.from(page.map(vehicleMapper::toResponse));
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.VEHICLE_SUMMARY_CACHE, allEntries = true)
    public VehicleResponse update(UUID id, VehicleUpdateRequest request) {
        log.info("Updating vehicle with id {}", id);
        VehicleEntity vehicle = findVehicleOrThrow(id);

        if (request.licensePlate() != null && !request.licensePlate().equals(vehicle.getLicensePlate())
                && vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            log.warn("Vehicle update rejected, duplicate license plate {}", request.licensePlate());
            throw new DuplicateResourceException(
                    "A vehicle with license plate '" + request.licensePlate() + "' already exists");
        }

        vehicleMapper.updateEntityFromRequest(request, vehicle);

        if (request.assignedDriverId() != null) {
            vehicle.setAssignedDriver(resolveActiveDriver(request.assignedDriverId()));
            log.info("Vehicle {} assigned to driver {}", id, request.assignedDriverId());
        }

        vehicleRepository.save(vehicle);
        log.info("Vehicle {} updated successfully", id);

        return vehicleMapper.toResponse(vehicle);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.VEHICLE_SUMMARY_CACHE, allEntries = true)
    public void delete(UUID id) {
        log.info("Deleting vehicle with id {}", id);
        VehicleEntity vehicle = findVehicleOrThrow(id);
        vehicleRepository.delete(vehicle);
        log.info("Vehicle {} deleted successfully", id);
    }

    @Override
    @Transactional
    public void updateLocation(UUID vehicleId, double latitude, double longitude) {
        log.debug("Updating location for vehicle {} to ({}, {})", vehicleId, latitude, longitude);
        VehicleEntity vehicle = findVehicleOrThrow(vehicleId);
        vehicle.setLastLatitude(latitude);
        vehicle.setLastLongitude(longitude);
        vehicle.setLastLocationAt(Instant.now());
        vehicleRepository.save(vehicle);
    }

    @Cacheable(value = RedisConfig.VEHICLE_SUMMARY_CACHE, key = "'all'")
    public List<com.fleettrack.dto.response.VehicleSummaryResponse> getAllSummariesCached() {
        log.debug("Loading vehicle summaries from database, cache miss");
        return vehicleRepository.findAll().stream().map(vehicleMapper::toSummaryResponse).toList();
    }

    private VehicleEntity findVehicleOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Vehicle not found with id {}", id);
                    return new ResourceNotFoundException("Vehicle not found with id: " + id);
                });
    }

    private DriverEntity resolveActiveDriver(UUID driverId) {
        DriverEntity driver = driverRepository.findById(driverId)
                .orElseThrow(() -> {
                    log.warn("Driver not found with id {}", driverId);
                    return new ResourceNotFoundException("Driver not found with id: " + driverId);
                });
        if (!driver.isActive()) {
            log.warn("Assignment rejected, driver {} is inactive", driverId);
            throw new BusinessRuleException("Cannot assign an inactive driver to a vehicle");
        }
        return driver;
    }
}