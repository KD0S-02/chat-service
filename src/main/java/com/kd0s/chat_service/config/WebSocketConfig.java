package com.kd0s.chat_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.kd0s.chat_service.handler.SocketHandler;
import com.kd0s.chat_service.services.GroupService;
import com.kd0s.chat_service.services.UserGroupService;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final UserGroupService userGroupService;
    private final GroupService groupService;

    public WebSocketConfig(UserGroupService userGroupService, GroupService groupService) {
        this.userGroupService = userGroupService;
        this.groupService = groupService;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(socketHandler(), "/chat")
                .setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler socketHandler() {
        return new SocketHandler(userGroupService, groupService);
    }
}
