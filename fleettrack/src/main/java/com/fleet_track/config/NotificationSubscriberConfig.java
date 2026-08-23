package com.fleet_track.config;

import com.fleet_track.dto.response.NotificationMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class NotificationSubscriberConfig {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public MessageListenerAdapter notificationListenerAdapter() {
        return new MessageListenerAdapter((org.springframework.data.redis.connection.MessageListener) (message, pattern) -> {
            try {
                NotificationMessage payload = objectMapper.readValue(message.getBody(), NotificationMessage.class);
                messagingTemplate.convertAndSend("/topic/notifications", payload);
            } catch (Exception e) {
                log.warn("Failed to relay notification to WebSocket clients: {}", e.getMessage());
            }
        });
    }

    @Bean
    public RedisMessageListenerContainer notificationContainer(RedisConnectionFactory connectionFactory,
                                                               MessageListenerAdapter notificationListenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(notificationListenerAdapter, new PatternTopic(RedisConfig.NOTIFICATIONS_CHANNEL));
        return container;
    }
}