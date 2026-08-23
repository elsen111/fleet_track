package com.fleet_track.controller;

import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.request.DriverUpdateRequest;
import com.fleet_track.dto.response.ApiResponse;
import com.fleet_track.dto.response.DriverResponse;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.service.DriverService;
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
@RequestMapping("/api/v1/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Driver profile administration")
public class DriverController {

    private final DriverService driverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<DriverResponse>> create(@Valid @RequestBody DriverCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created", driverService.create(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(driverService.getById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<DriverResponse>>> search(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Pageable pageable = PageableUtils.build(page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success(driverService.search(active, search, pageable)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<DriverResponse>> update(@PathVariable UUID id,
                                                              @Valid @RequestBody DriverUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Driver updated", driverService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        driverService.delete(id);
        return ResponseEntity.ok(ApiResponse.message("Driver deleted"));
    }
}