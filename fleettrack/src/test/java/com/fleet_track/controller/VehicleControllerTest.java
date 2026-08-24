package com.fleet_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet_track.dto.request.VehicleCreateRequest;
import com.fleet_track.dto.response.PagedResponse;
import com.fleet_track.dto.response.VehicleResponse;
import com.fleet_track.enums.VehicleStatus;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
@AutoConfigureMockMvc(addFilters = false)
class VehicleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private VehicleService vehicleService;

    @Test
    void create_returns201AndVehicle_whenRequestIsValid() throws Exception {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Toyota", "Hilux", 2022, "10-AA-123", null, VehicleStatus.ACTIVE, 0, null);
        VehicleResponse response = VehicleResponse.builder()
                .id(UUID.randomUUID()).make("Toyota").model("Hilux").year(2022)
                .licensePlate("10-AA-123").status(VehicleStatus.ACTIVE)
                .createdAt(Instant.now()).updatedAt(Instant.now()).build();

        when(vehicleService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.licensePlate").value("10-AA-123"));
    }

    @Test
    void create_returns400_whenLicensePlateIsBlank() throws Exception {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Toyota", "Hilux", 2022, "", null, VehicleStatus.ACTIVE, 0, null);

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors").isArray());
    }

    @Test
    void create_returns409_whenLicensePlateAlreadyExists() throws Exception {
        VehicleCreateRequest request = new VehicleCreateRequest(
                "Toyota", "Hilux", 2022, "10-AA-123", null, VehicleStatus.ACTIVE, 0, null);

        when(vehicleService.create(any())).thenThrow(
                new DuplicateResourceException("A vehicle with license plate '10-AA-123' already exists"));

        mockMvc.perform(post("/api/v1/vehicles")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    void getById_returns200AndVehicle_whenVehicleExists() throws Exception {
        UUID id = UUID.randomUUID();
        VehicleResponse response = VehicleResponse.builder()
                .id(id).make("Ford").model("Transit").year(2023)
                .licensePlate("20-BB-456").status(VehicleStatus.ACTIVE).build();

        when(vehicleService.getById(id)).thenReturn(response);

        mockMvc.perform(get("/api/v1/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()));
    }

    @Test
    void getById_returns404_whenVehicleDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(vehicleService.getById(id)).thenThrow(
                new ResourceNotFoundException("Vehicle not found with id: " + id));

        mockMvc.perform(get("/api/v1/vehicles/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void search_returns200AndPagedResults() throws Exception {
        PagedResponse<VehicleResponse> paged = PagedResponse.<VehicleResponse>builder()
                .content(java.util.List.of())
                .pageNumber(0).pageSize(20).totalElements(0).totalPages(0).last(true).build();

        when(vehicleService.search(any(), any(), any(), any(), any())).thenReturn(paged);

        mockMvc.perform(get("/api/v1/vehicles").param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void delete_returns200_whenVehicleIsDeleted() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/vehicles/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Vehicle deleted"));
    }
}