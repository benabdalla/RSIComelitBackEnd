package com.rsi.comelit.chat;

import com.rsi.comelit.service.UserServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@AllArgsConstructor
public class ChatPresenceEventListener {
    private final UserServiceImpl userService;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        if (userId != null) {
            userService.setUserStatus(Long.valueOf(userId), true);
            messagingTemplate.convertAndSend("/topic/users", userService.getAllChatUsers());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor sha = StompHeaderAccessor.wrap(event.getMessage());
        String userId = sha.getUser() != null ? sha.getUser().getName() : null;
        if (userId != null) {
            userService.setUserStatus(Long.valueOf(userId), false);
            messagingTemplate.convertAndSend("/topic/users", userService.getAllChatUsers());
        }
    }
}

