package com.fleet_track.service;

import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.request.DriverUpdateRequest;
import com.fleet_track.dto.response.DriverResponse;
import com.fleet_track.dto.response.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DriverService {
    DriverResponse create(DriverCreateRequest request);
    DriverResponse getById(UUID id);
    PagedResponse<DriverResponse> search(Boolean active, String search, Pageable pageable);
    DriverResponse update(UUID id, DriverUpdateRequest request);
    void delete(UUID id);
}