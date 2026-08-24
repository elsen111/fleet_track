package com.fleet_track.service;

import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.request.DriverUpdateRequest;
import com.fleet_track.entity.DriverEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.LicenseType;
import com.fleet_track.exception.BusinessRuleException;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.DriverMapper;
import com.fleet_track.repository.DriverRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.impl.DriverServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceImplTest {

    @Mock private DriverRepository driverRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private DriverMapper driverMapper;

    private DriverServiceImpl driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverServiceImpl(driverRepository, vehicleRepository, driverMapper);
    }

    @Test
    void create_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        DriverCreateRequest request = new DriverCreateRequest(
                "Elshan Hasanov", "elshan@fleettrack.com", "+994501234567", "LN123",
                LicenseType.B, LocalDate.now().plusYears(2));
        when(driverRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> driverService.create(request))
                .isInstanceOf(DuplicateResourceException.class);

        verify(driverRepository, never()).save(any());
    }

    @Test
    void create_throwsDuplicateResourceException_whenLicenseNumberAlreadyExists() {
        DriverCreateRequest request = new DriverCreateRequest(
                "Elshan Hasanov", "elshan@fleettrack.com", "+994501234567", "LN123",
                LicenseType.B, LocalDate.now().plusYears(2));
        when(driverRepository.existsByEmail(request.email())).thenReturn(false);
        when(driverRepository.existsByLicenseNumber("LN123")).thenReturn(true);

        assertThatThrownBy(() -> driverService.create(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getById_throwsResourceNotFoundException_whenDriverDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(driverRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> driverService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_throwsBusinessRuleException_whenDriverIsAssignedToAVehicle() {
        UUID id = UUID.randomUUID();
        DriverEntity driver = DriverEntity.builder().id(id).build();
        VehicleEntity assignedVehicle = VehicleEntity.builder().id(UUID.randomUUID()).build();

        when(driverRepository.findById(id)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByAssignedDriverId(id)).thenReturn(Optional.of(assignedVehicle));

        assertThatThrownBy(() -> driverService.delete(id))
                .isInstanceOf(BusinessRuleException.class);

        verify(driverRepository, never());
    }

    @Test
    void delete_removesDriver_whenNotAssignedToAnyVehicle() {
        UUID id = UUID.randomUUID();
        DriverEntity driver = DriverEntity.builder().id(id).build();

        when(driverRepository.findById(id)).thenReturn(Optional.of(driver));
        when(vehicleRepository.findByAssignedDriverId(id)).thenReturn(Optional.empty());

        driverService.delete(id);

        verify(driverRepository).delete(driver);
    }

    @Test
    void update_throwsDuplicateResourceException_whenNewEmailAlreadyUsedByAnotherDriver() {
        UUID id = UUID.randomUUID();
        DriverEntity existing = DriverEntity.builder().id(id).email("old@fleettrack.com").build();
        DriverUpdateRequest request = new DriverUpdateRequest(
                null, "new@fleettrack.com", null, null, null, null, null);

        when(driverRepository.findById(id)).thenReturn(Optional.of(existing));
        when(driverRepository.existsByEmail("new@fleettrack.com")).thenReturn(true);

        assertThatThrownBy(() -> driverService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }
}