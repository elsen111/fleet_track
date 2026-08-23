package com.fleet_track.service;

import com.fleet_track.dto.response.NotificationMessage;

public interface NotificationService {
    void publish(NotificationMessage message);
}