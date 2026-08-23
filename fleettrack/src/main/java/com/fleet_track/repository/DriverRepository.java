package com.fleet_track.repository;

import com.fleet_track.entity.DriverEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface DriverRepository extends JpaRepository<DriverEntity, UUID>,
        JpaSpecificationExecutor<DriverEntity> {

    boolean existsByEmail(String email);
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<DriverEntity> findByEmail(String email);
}