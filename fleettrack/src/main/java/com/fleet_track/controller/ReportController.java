package com.fleet_track.controller;

import com.fleet_track.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Downloadable PDF reports for fleet status, maintenance logs, and driver activity")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Download fleet status report", description = "Generates a PDF summarizing every vehicle's status, assigned driver, and odometer reading.")
    @GetMapping("/fleet-status")
    public ResponseEntity<byte[]> fleetStatusReport() {
        byte[] pdf = reportService.generateFleetStatusReport();
        return buildPdfResponse(pdf, "fleet-status-report.pdf");
    }

    @Operation(summary = "Download maintenance log report", description = "Generates a PDF of the full maintenance history for a specific vehicle.")
    @GetMapping("/maintenance-log/{vehicleId}")
    public ResponseEntity<byte[]> maintenanceLogReport(@PathVariable UUID vehicleId) {
        byte[] pdf = reportService.generateMaintenanceLogReport(vehicleId);
        return buildPdfResponse(pdf, "maintenance-log-" + vehicleId + ".pdf");
    }

    @Operation(summary = "Download driver activity report", description = "Generates a PDF summarizing every driver's status, license, and vehicle assignment.")
    @GetMapping("/driver-activity")
    public ResponseEntity<byte[]> driverActivityReport() {
        byte[] pdf = reportService.generateDriverActivityReport();
        return buildPdfResponse(pdf, "driver-activity-report.pdf");
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(pdf);
    }
}