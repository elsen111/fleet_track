package com.fleet_track.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet_track.dto.request.MaintenanceRecordRequest;
import com.fleet_track.dto.response.MaintenanceRecordResponse;
import com.fleet_track.enums.MaintenanceType;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.service.MaintenanceRecordService;
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

@WebMvcTest(MaintenanceRecordController.class)
@AutoConfigureMockMvc(addFilters = false)
class MaintenanceRecordControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private MaintenanceRecordService maintenanceRecordService;

    @Test
    void create_returns201_whenRequestIsValid() throws Exception {
        UUID vehicleId = UUID.randomUUID();
        MaintenanceRecordRequest request = new MaintenanceRecordRequest(
                vehicleId, MaintenanceType.OIL_CHANGE, LocalDate.now(), null, null, null, null);
        MaintenanceRecordResponse response = MaintenanceRecordResponse.builder()
                .id(UUID.randomUUID()).vehicleId(vehicleId).maintenanceType(MaintenanceType.OIL_CHANGE)
                .serviceDate(LocalDate.now()).build();

        when(maintenanceRecordService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/maintenance-records")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.maintenanceType").value("OIL_CHANGE"));
    }

    @Test
    void create_returns400_whenServiceDateIsInTheFuture() throws Exception {
        MaintenanceRecordRequest request = new MaintenanceRecordRequest(
                UUID.randomUUID(), MaintenanceType.OIL_CHANGE, LocalDate.now().plusDays(5),
                null, null, null, null);

        mockMvc.perform(post("/api/v1/maintenance-records")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByVehicle_returns404_whenVehicleDoesNotExist() throws Exception {
        UUID vehicleId = UUID.randomUUID();
        when(maintenanceRecordService.getByVehicle(any(), any())).thenThrow(
                new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        mockMvc.perform(get("/api/v1/maintenance-records/vehicle/{vehicleId}", vehicleId))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returns200_whenRecordIsDeleted() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/maintenance-records/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Maintenance record deleted"));
    }
}