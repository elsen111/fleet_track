package com.fleet_track.service.impl;

import com.fleet_track.config.RedisConfig;
import com.fleet_track.dto.response.NotificationMessage;
import com.fleet_track.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void publish(NotificationMessage message) {
        log.info("Publishing notification type={} for vehicle {}", message.type(), message.vehicleId());

        try {
            redisTemplate.convertAndSend(RedisConfig.NOTIFICATIONS_CHANNEL, message);
            log.debug("Notification published successfully to channel {}", RedisConfig.NOTIFICATIONS_CHANNEL);
        } catch (Exception e) {
            log.warn("Failed to publish notification for vehicle {}: {}", message.vehicleId(), e.getMessage());
        }
    }
}