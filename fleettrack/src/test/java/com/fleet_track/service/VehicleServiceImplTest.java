package com.fleet_track.service;

import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.request.VehicleUpdateRequest;
import com.fleet_track.entity.DriverEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.exception.BusinessRuleException;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.VehicleMapper;
import com.fleet_track.repository.DriverRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.impl.VehicleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceImplTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private VehicleMapper vehicleMapper;

    private VehicleServiceImpl vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleServiceImpl(vehicleRepository, driverRepository, vehicleMapper);
    }

    @Test
    void create_throwsDuplicateResourceException_whenLicensePlateExists() {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Toyota", "Hilux", 2022, "10-AA-123", null, VehicleStatus.ACTIVE, 0, null);
        when(vehicleRepository.existsByLicensePlate("10-AA-123")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("10-AA-123");

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void create_throwsDuplicateResourceException_whenVinExists() {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Ford", "Transit", 2023, "20-BB-456", "VIN123", VehicleStatus.ACTIVE, 0, null);
        when(vehicleRepository.existsByLicensePlate("20-BB-456")).thenReturn(false);
        when(vehicleRepository.existsByVin("VIN123")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.create(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("VIN123");
    }

    @Test
    void create_throwsBusinessRuleException_whenAssignedDriverIsInactive() {
        UUID driverId = UUID.randomUUID();
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Toyota", "Hilux", 2022, "30-CC-789", null, VehicleStatus.ACTIVE, 0, driverId);
        DriverEntity inactiveDriver = DriverEntity.builder().id(driverId).active(false).build();

        when(vehicleRepository.existsByLicensePlate("30-CC-789")).thenReturn(false);
        when(vehicleMapper.toEntity(request)).thenReturn(new VehicleEntity());
        when(driverRepository.findById(driverId)).thenReturn(Optional.of(inactiveDriver));

        assertThatThrownBy(() -> vehicleService.create(request))
                .isInstanceOf(BusinessRuleException.class);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getById_throwsResourceNotFoundException_whenVehicleDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void update_throwsDuplicateResourceException_whenNewLicensePlateAlreadyUsedByAnotherVehicle() {
        UUID id = UUID.randomUUID();
        VehicleEntity existing = VehicleEntity.builder().id(id).licensePlate("OLD-123").build();
        VehicleUpdateRequest request = new VehicleUpdateRequest(
                null, null, null, "NEW-456", null, null, null, null);

        when(vehicleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(vehicleRepository.existsByLicensePlate("NEW-456")).thenReturn(true);

        assertThatThrownBy(() -> vehicleService.update(id, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void delete_removesVehicle_whenVehicleExists() {
        UUID id = UUID.randomUUID();
        VehicleEntity vehicle = VehicleEntity.builder().id(id).build();
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));

        vehicleService.delete(id);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    void delete_throwsResourceNotFoundException_whenVehicleDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(vehicleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(vehicleRepository, never()).delete(any(VehicleEntity.class));
    }

    @Test
    void updateLocation_savesNewCoordinates_whenVehicleExists() {
        UUID id = UUID.randomUUID();
        VehicleEntity vehicle = VehicleEntity.builder().id(id).licensePlate("10-AA-123").build();
        when(vehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));

        vehicleService.updateLocation(id, 40.4093, 49.8671);

        verify(vehicleRepository).save(argThat(v ->
                v.getLastLatitude().equals(40.4093) && v.getLastLongitude().equals(49.8671)));
    }
}