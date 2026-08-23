package com.fleet_track.repository;

import com.fleet_track.entity.LocationUpdateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocationUpdateRepository extends JpaRepository<LocationUpdateEntity, UUID> {
    Optional<LocationUpdateEntity> findFirstByVehicleIdOrderByRecordedAtDesc(UUID vehicleId);
}