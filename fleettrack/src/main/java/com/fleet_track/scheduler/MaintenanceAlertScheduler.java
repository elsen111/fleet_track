package com.fleet_track.scheduler;

import com.fleet_track.dto.response.NotificationMessage;
import com.fleet_track.entity.MaintenanceRecordEntity;
import com.fleet_track.enums.NotificationType;
import com.fleet_track.repository.MaintenanceRecordRepository;
import com.fleet_track.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaintenanceAlertScheduler {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 7 * * *")
    public void checkUpcomingAndOverdueMaintenance() {
        LocalDate today = LocalDate.now();

        List<MaintenanceRecordEntity> upcoming =
                maintenanceRecordRepository.findByNextDueDateBetween(today, today.plusDays(7));
        upcoming.forEach(record -> notify(record, NotificationType.MAINTENANCE_DUE,
                "Maintenance due on " + record.getNextDueDate()));

        List<MaintenanceRecordEntity> overdue =
                maintenanceRecordRepository.findByNextDueDateBefore(today);
        overdue.forEach(record -> notify(record, NotificationType.MAINTENANCE_OVERDUE,
                "Maintenance overdue since " + record.getNextDueDate()));

        log.info("Maintenance alert scan complete: {} upcoming, {} overdue", upcoming.size(), overdue.size());
    }

    private void notify(MaintenanceRecordEntity record, NotificationType type, String message) {
        notificationService.publish(NotificationMessage.builder()
                .type(type)
                .vehicleId(record.getVehicle().getId())
                .licensePlate(record.getVehicle().getLicensePlate())
                .message(message)
                .timestamp(Instant.now())
                .build());
    }
}