package com.fleet_track.service.impl;

import com.fleet_track.entity.DriverEntity;
import com.fleet_track.entity.MaintenanceRecordEntity;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.repository.DriverRepository;
import com.fleet_track.repository.MaintenanceRecordRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.ReportService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    @Override
    @Transactional(readOnly = true)
    public byte[] generateFleetStatusReport() {
        log.info("Generating fleet status report");

        List<VehicleEntity> vehicles = vehicleRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
             Document document = new Document(pdfDocument, PageSize.A4)) {

            document.add(buildTitle("FleetTrack — Fleet Status Report"));
            document.add(buildSubtitle("Generated on " + LocalDate.now().format(DATE_FORMAT)
                    + " — " + vehicles.size() + " vehicles"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{18, 18, 10, 15, 15, 12, 12}))
                    .useAllAvailableWidth();
            addHeaderRow(table, "Plate", "Make / Model", "Year", "Status", "Driver", "Odometer (km)", "Updated");

            for (VehicleEntity vehicle : vehicles) {
                table.addCell(dataCell(vehicle.getLicensePlate()));
                table.addCell(dataCell(vehicle.getMake() + " " + vehicle.getModel()));
                table.addCell(dataCell(String.valueOf(vehicle.getYear())));
                table.addCell(dataCell(vehicle.getStatus().name()));
                table.addCell(dataCell(vehicle.getAssignedDriver() != null
                        ? vehicle.getAssignedDriver().getFullName() : "Unassigned"));
                table.addCell(dataCell(String.valueOf(vehicle.getOdometerKm())));
                table.addCell(dataCell(vehicle.getUpdatedAt() != null
                        ? vehicle.getUpdatedAt().toString().substring(0, 10) : "-"));
            }

            document.add(table);
        }

        log.info("Fleet status report generated, {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMaintenanceLogReport(UUID vehicleId) {
        log.info("Generating maintenance log report for vehicle {}", vehicleId);

        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> {
                    log.warn("Maintenance log report rejected, vehicle not found with id {}", vehicleId);
                    return new ResourceNotFoundException("Vehicle not found with id: " + vehicleId);
                });

        List<MaintenanceRecordEntity> records =
                maintenanceRecordRepository.findByVehicleIdOrderByServiceDateDesc(vehicleId);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
             Document document = new Document(pdfDocument, PageSize.A4)) {

            document.add(buildTitle("FleetTrack — Maintenance Log"));
            document.add(buildSubtitle(vehicle.getMake() + " " + vehicle.getModel()
                    + " (" + vehicle.getLicensePlate() + ") — " + records.size() + " records"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{18, 14, 14, 12, 12, 30}))
                    .useAllAvailableWidth();
            addHeaderRow(table, "Type", "Service Date", "Next Due", "Odometer", "Cost", "Notes");

            for (MaintenanceRecordEntity record : records) {
                table.addCell(dataCell(record.getMaintenanceType().name()));
                table.addCell(dataCell(record.getServiceDate().format(DATE_FORMAT)));
                table.addCell(dataCell(record.getNextDueDate() != null
                        ? record.getNextDueDate().format(DATE_FORMAT) : "-"));
                table.addCell(dataCell(record.getOdometerKm() != null
                        ? record.getOdometerKm().toString() : "-"));
                table.addCell(dataCell(record.getCost() != null ? record.getCost().toString() : "-"));
                table.addCell(dataCell(record.getNotes() != null ? record.getNotes() : "-"));
            }

            document.add(table);
        }

        log.info("Maintenance log report generated for vehicle {}, {} bytes", vehicleId, outputStream.size());
        return outputStream.toByteArray();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateDriverActivityReport() {
        log.info("Generating driver activity report");

        List<DriverEntity> drivers = driverRepository.findAll();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (PdfDocument pdfDocument = new PdfDocument(new PdfWriter(outputStream));
             Document document = new Document(pdfDocument, PageSize.A4)) {

            document.add(buildTitle("FleetTrack — Driver Activity Report"));
            document.add(buildSubtitle("Generated on " + LocalDate.now().format(DATE_FORMAT)
                    + " — " + drivers.size() + " drivers"));

            Table table = new Table(UnitValue.createPercentArray(new float[]{22, 25, 15, 15, 10, 13}))
                    .useAllAvailableWidth();
            addHeaderRow(table, "Full Name", "Email", "License No.", "License Expiry", "Active", "Assigned Vehicle");

            for (DriverEntity driver : drivers) {
                VehicleEntity assigned = vehicleRepository.findByAssignedDriverId(driver.getId()).orElse(null);

                table.addCell(dataCell(driver.getFullName()));
                table.addCell(dataCell(driver.getEmail()));
                table.addCell(dataCell(driver.getLicenseNumber()));
                table.addCell(dataCell(driver.getLicenseExpiry().format(DATE_FORMAT)));
                table.addCell(dataCell(driver.isActive() ? "Yes" : "No"));
                table.addCell(dataCell(assigned != null ? assigned.getLicensePlate() : "Unassigned"));
            }

            document.add(table);
        }

        log.info("Driver activity report generated, {} bytes", outputStream.size());
        return outputStream.toByteArray();
    }

    private Paragraph buildTitle(String text) {
        return new Paragraph(text).setBold().setFontSize(18).setMarginBottom(2);
    }

    private Paragraph buildSubtitle(String text) {
        return new Paragraph(text).setFontSize(10).setFontColor(ColorConstants.GRAY).setMarginBottom(16);
    }

    private void addHeaderRow(Table table, String... headers) {
        for (String header : headers) {
            table.addHeaderCell(new Cell()
                    .add(new Paragraph(header).setBold().setFontSize(9))
                    .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(5));
        }
    }

    private Cell dataCell(String text) {
        return new Cell().add(new Paragraph(text).setFontSize(9)).setPadding(5);
    }
}