package com.fleet_track.service;

import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.entity.MaintenanceRecordEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.enums.MaintenanceType;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.mapper.MaintenanceRecordMapper;
import com.fleet_track.repository.MaintenanceRecordRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.impl.MaintenanceRecordServiceImpl;
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
class MaintenanceRecordServiceImplTest {

    @Mock private MaintenanceRecordRepository maintenanceRecordRepository;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private MaintenanceRecordMapper maintenanceRecordMapper;

    private MaintenanceRecordServiceImpl maintenanceRecordService;

    @BeforeEach
    void setUp() {
        maintenanceRecordService = new MaintenanceRecordServiceImpl(
                maintenanceRecordRepository, vehicleRepository, maintenanceRecordMapper);
    }

    @Test
    void create_throwsResourceNotFoundException_whenVehicleDoesNotExist() {
        UUID vehicleId = UUID.randomUUID();
        MaintenanceRecordRequest request = new MaintenanceRecordRequest(
                vehicleId, MaintenanceType.OIL_CHANGE, LocalDate.now(), null, null, null, null);
        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceRecordService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(maintenanceRecordRepository, never()).save(any());
    }

    @Test
    void create_setsVehicleStatusToInMaintenance_whenVehicleWasActive() {
        UUID vehicleId = UUID.randomUUID();
        VehicleEntity vehicle = VehicleEntity.builder().id(vehicleId).status(VehicleStatus.ACTIVE).build();
        MaintenanceRecordRequest request = new MaintenanceRecordRequest(
                vehicleId, MaintenanceType.BRAKE_SERVICE, LocalDate.now(), null, null, null, null);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(maintenanceRecordMapper.toEntity(request)).thenReturn(new MaintenanceRecordEntity());

        maintenanceRecordService.create(request);

        verify(vehicleRepository).save(argThat(v -> v.getStatus() == VehicleStatus.IN_MAINTENANCE));
    }

    @Test
    void create_doesNotChangeVehicleStatus_whenVehicleAlreadyOutOfService() {
        UUID vehicleId = UUID.randomUUID();
        VehicleEntity vehicle = VehicleEntity.builder().id(vehicleId).status(VehicleStatus.OUT_OF_SERVICE).build();
        MaintenanceRecordRequest request = new MaintenanceRecordRequest(
                vehicleId, MaintenanceType.ENGINE_REPAIR, LocalDate.now(), null, null, null, null);

        when(vehicleRepository.findById(vehicleId)).thenReturn(Optional.of(vehicle));
        when(maintenanceRecordMapper.toEntity(request)).thenReturn(new MaintenanceRecordEntity());

        maintenanceRecordService.create(request);

        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void getByVehicle_throwsResourceNotFoundException_whenVehicleDoesNotExist() {
        UUID vehicleId = UUID.randomUUID();
        when(vehicleRepository.existsById(vehicleId)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceRecordService.getByVehicle(vehicleId, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_throwsResourceNotFoundException_whenRecordDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(maintenanceRecordRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceRecordService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}