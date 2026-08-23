package com.fleet_track.controller;

import com.fleet_track.dto.request.LocationUpdateRequest;
import com.fleet_track.dto.response.LocationBroadcastResponse;
import com.fleet_track.entity.VehicleEntity;
import com.fleet_track.exception.ResourceNotFoundException;
import com.fleet_track.repository.LocationUpdateRepository;
import com.fleet_track.repository.VehicleRepository;
import com.fleet_track.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class VehicleLocationWebSocketController {

    private final VehicleService vehicleService;
    private final VehicleRepository vehicleRepository;
    private final LocationUpdateRepository locationUpdateRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Clients send GPS pings to /app/location.update; broadcast goes out on /topic/vehicles/{id}/location
    @MessageMapping("/location.update")
    public void handleLocationUpdate(LocationUpdateRequest request) {
        UUID vehicleId = UUID.fromString(request.vehicleId());

        VehicleEntity vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));

        vehicleService.updateLocation(vehicleId, request.latitude(), request.longitude());

        Instant now = Instant.now();
        locationUpdateRepository.save(com.fleet_track.entity.LocationUpdateEntity.builder()
                .vehicle(vehicle)
                .latitude(request.latitude())
                .longitude(request.longitude())
                .recordedAt(now)
                .build());

        LocationBroadcastResponse payload = LocationBroadcastResponse.builder()
                .vehicleId(vehicleId)
                .licensePlate(vehicle.getLicensePlate())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .recordedAt(now)
                .build();

        messagingTemplate.convertAndSend("/topic/vehicles/" + vehicleId + "/location", payload);
        messagingTemplate.convertAndSend("/topic/vehicles/location", payload); // fleet-wide dashboard feed
    }
}