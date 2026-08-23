package com.fleet_track.repository;

import com.fleet_track.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<VehicleEntity, UUID>,
        JpaSpecificationExecutor<VehicleEntity> {

    boolean existsByLicensePlate(String licensePlate);

    boolean existsByVin(String vin);

    Optional<VehicleEntity> findByLicensePlate(String licensePlate);

    Optional<VehicleEntity> findByAssignedDriverId(UUID driverId);
}