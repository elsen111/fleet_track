package com.fleet_track.controller;

import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.request.VehicleUpdateRequest;
import com.fleet_track.dto.response.ApiResponse;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.dto.response.VehicleResponse;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.service.VehicleService;
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
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle registration and fleet management")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<VehicleResponse>> create(@Valid @RequestBody VehicleCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created", vehicleService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<VehicleResponse>>> search(
            @RequestParam(required = false) VehicleStatus status,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageableUtils.build(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(
                vehicleService.search(status, year, driverId, search, pageable)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<VehicleResponse>> update(@PathVariable UUID id,
                                                               @Valid @RequestBody VehicleUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated", vehicleService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        vehicleService.delete(id);
        return ResponseEntity.ok(ApiResponse.message("Vehicle deleted"));
    }
}