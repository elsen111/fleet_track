package com.fleet_track.controller;

import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.dto.response.ApiResponse;
import com.fleet_track.dto.response.MaintenanceRecordResponse;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.service.MaintenanceRecordService;
import com.fleet_track.util.PageableUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-records")
@RequiredArgsConstructor
@Tag(name = "Maintenance", description = "Vehicle maintenance and service history")
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<MaintenanceRecordResponse>> create(
            @Valid @RequestBody MaintenanceRecordRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance record created", maintenanceRecordService.create(request)));
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<ApiResponse<PagedResponse<MaintenanceRecordResponse>>> getByVehicle(
            @PathVariable UUID vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "serviceDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageableUtils.build(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(
                maintenanceRecordService.getByVehicle(vehicleId, pageable)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        maintenanceRecordService.delete(id);
        return ResponseEntity.ok(ApiResponse.message("Maintenance record deleted"));
    }
}