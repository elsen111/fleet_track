package com.fleet_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet_track.dto.request.DriverCreateRequest;
import com.fleet_track.dto.response.DriverResponse;
import com.fleet_track.enums.LicenseType;
import com.fleet_track.exception.DuplicateResourceException;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
@AutoConfigureMockMvc(addFilters = false)
class DriverControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private DriverService driverService;

    @Test
    void create_returns201AndDriver_whenRequestIsValid() throws Exception {
        DriverCreateRequest request = new DriverCreateRequest(
                "Elshan Hasanov", "elshan@fleettrack.com", "+994501234567", "LN123",
                LicenseType.B, LocalDate.now().plusYears(2));
        DriverResponse response = DriverResponse.builder()
                .id(UUID.randomUUID()).fullName("Elshan Hasanov").email("elshan@fleettrack.com")
                .licenseNumber("LN123").licenseType(LicenseType.B).active(true).build();

        when(driverService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("elshan@fleettrack.com"));
    }

    @Test
    void create_returns400_whenLicenseExpiryIsInThePast() throws Exception {
        DriverCreateRequest request = new DriverCreateRequest(
                "Elshan Hasanov", "elshan@fleettrack.com", "+994501234567", "LN123",
                LicenseType.B, LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_returns409_whenEmailAlreadyExists() throws Exception {
        DriverCreateRequest request = new DriverCreateRequest(
                "Elshan Hasanov", "elshan@fleettrack.com", "+994501234567", "LN123",
                LicenseType.B, LocalDate.now().plusYears(2));

        when(driverService.create(any())).thenThrow(
                new DuplicateResourceException("A driver with email 'elshan@fleettrack.com' already exists"));

        mockMvc.perform(post("/api/v1/drivers")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_returns404_whenDriverDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(driverService.getById(id)).thenThrow(
                new ResourceNotFoundException("Driver not found with id: " + id));

        mockMvc.perform(get("/api/v1/drivers/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns200_whenDriverIsDeleted() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/drivers/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Driver deleted"));
    }
}