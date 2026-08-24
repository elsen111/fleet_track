package com.fleet_track.service;

import com.fleet_track.config.RedisConfig;
import com.fleet_track.dto.response.NotificationMessage;
import com.fleet_track.enums.NotificationType;
import com.fleet_track.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private RedisTemplate<String, Object> redisTemplate;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(redisTemplate);
    }

    @Test
    void publish_sendsMessageToNotificationsChannel() {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.MAINTENANCE_DUE)
                .vehicleId(UUID.randomUUID())
                .licensePlate("10-AA-123")
                .message("Maintenance due")
                .timestamp(Instant.now())
                .build();

        notificationService.publish(message);

        verify(redisTemplate).convertAndSend(RedisConfig.NOTIFICATIONS_CHANNEL, message);
    }

    @Test
    void publish_doesNotThrow_whenRedisTemplateFails() {
        NotificationMessage message = NotificationMessage.builder()
                .type(NotificationType.VEHICLE_OFFLINE)
                .vehicleId(UUID.randomUUID())
                .licensePlate("20-BB-456")
                .message("Vehicle offline")
                .timestamp(Instant.now())
                .build();

        doThrow(new RuntimeException("Redis unavailable"))
                .when(redisTemplate).convertAndSend(anyString(), any());

        notificationService.publish(message);
    }
}