package com.fleet_track.service.impl;

import com.fleet_track.config.RedisConfig;
import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.request.DriverUpdateRequest;
import com.fleet_track.dto.response.DriverResponse;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.entity.DriverEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.exception.BusinessRuleException;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.DriverMapper;
import com.fleet_track.repository.DriverRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.repository.specification.DriverSpecification;
import com.fleet_track.service.DriverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverMapper driverMapper;

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.DRIVER_SUMMARY_CACHE, allEntries = true)
    public DriverResponse create(DriverCreateRequest request) {
        log.info("Creating driver with email {}", request.email());

        if (driverRepository.existsByEmail(request.email())) {
            log.warn("Driver creation rejected, duplicate email {}", request.email());
            throw new DuplicateResourceException("A driver with email '" + request.email() + "' already exists");
        }
        if (driverRepository.existsByLicenseNumber(request.licenseNumber())) {
            log.warn("Driver creation rejected, duplicate license number {}", request.licenseNumber());
            throw new DuplicateResourceException(
                    "A driver with license number '" + request.licenseNumber() + "' already exists");
        }

        DriverEntity driver = driverMapper.toEntity(request);
        driver.setActive(true);
        driverRepository.save(driver);
        log.info("Driver created successfully with id {}", driver.getId());

        return enrichWithAssignedVehicle(driver);
    }

    @Override
    @Transactional(readOnly = true)
    public DriverResponse getById(UUID id) {
        log.debug("Fetching driver with id {}", id);
        return enrichWithAssignedVehicle(findDriverOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<DriverResponse> search(Boolean active, String search, Pageable pageable) {
        log.debug("Searching drivers with active={}, search={}, page={}", active, search, pageable);

        Page<DriverEntity> page = driverRepository.findAll(
                DriverSpecification.withFilters(active, search), pageable);

        log.debug("Driver search returned {} results out of {}", page.getNumberOfElements(), page.getTotalElements());
        return PagedResponse.from(page.map(this::enrichWithAssignedVehicle));
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.DRIVER_SUMMARY_CACHE, allEntries = true)
    public DriverResponse update(UUID id, DriverUpdateRequest request) {
        log.info("Updating driver with id {}", id);
        DriverEntity driver = findDriverOrThrow(id);

        if (request.email() != null && !request.email().equals(driver.getEmail())
                && driverRepository.existsByEmail(request.email())) {
            log.warn("Driver update rejected, duplicate email {}", request.email());
            throw new DuplicateResourceException("A driver with email '" + request.email() + "' already exists");
        }

        driverMapper.updateEntityFromRequest(request, driver);
        driverRepository.save(driver);
        log.info("Driver {} updated successfully", id);

        return enrichWithAssignedVehicle(driver);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.DRIVER_SUMMARY_CACHE, allEntries = true)
    public void delete(UUID id) {
        log.info("Deleting driver with id {}", id);
        DriverEntity driver = findDriverOrThrow(id);

        boolean hasVehicle = vehicleRepository.findByAssignedDriverId(id).isPresent();
        if (hasVehicle) {
            log.warn("Driver deletion rejected, driver {} is currently assigned to a vehicle", id);
            throw new BusinessRuleException("Cannot delete a driver who is currently assigned to a vehicle");
        }

        driverRepository.delete(driver);
        log.info("Driver {} deleted successfully", id);
    }

    private DriverEntity findDriverOrThrow(UUID id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Driver not found with id {}", id);
                    return new ResourceNotFoundException("Driver not found with id: " + id);
                });
    }

    private DriverResponse enrichWithAssignedVehicle(DriverEntity driver) {
        DriverResponse base = driverMapper.toResponse(driver);
        Optional<VehicleEntity> assigned = vehicleRepository.findByAssignedDriverId(driver.getId());

        return assigned.map(v -> new DriverResponse(
                        base.id(), base.fullName(), base.email(), base.phone(), base.licenseNumber(),
                        base.licenseType(), base.licenseExpiry(), base.active(), v.getId(), v.getLicensePlate()))
                .orElse(base);
    }
}