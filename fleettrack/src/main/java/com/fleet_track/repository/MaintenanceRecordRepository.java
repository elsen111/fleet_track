package com.fleet_track.repository;

import com.fleet_track.entity.MaintenanceRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecordEntity, UUID> {

    Page<MaintenanceRecordEntity> findByVehicleId(UUID vehicleId, Pageable pageable);

    List<MaintenanceRecordEntity> findByNextDueDateBetween(LocalDate start, LocalDate end);

    List<MaintenanceRecordEntity> findByNextDueDateBefore(LocalDate date);
}