package com.fleet_track.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "fleettrack.rate-limit")
public class RateLimitProperties {
    private int capacity;
    private int refillTokens;
    private int refillDurationSeconds;
}