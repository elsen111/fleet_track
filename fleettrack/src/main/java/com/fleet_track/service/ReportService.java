package com.fleet_track.service;

import java.util.UUID;

public interface ReportService {
    byte[] generateFleetStatusReport();
    byte[] generateMaintenanceLogReport(UUID vehicleId);
    byte[] generateDriverActivityReport();
}