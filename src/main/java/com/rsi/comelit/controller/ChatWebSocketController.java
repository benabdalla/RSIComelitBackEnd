package com.rsi.comelit.controller;

import com.rsi.comelit.entity.Message;
import com.rsi.comelit.enumeration.NotificationType;
import com.rsi.comelit.service.MessageService;
import com.rsi.comelit.service.NotificationService;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@AllArgsConstructor
public class ChatWebSocketController {
    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message) {
        // Générer un ID et timestamp si absent
        message.setTimestamp(Instant.now());
        messageService.saveMessage(message);
        // Créer une notification de type MESSAGE
        notificationService.sendNotification(
                message.getRecipient(),
                message.getSender(),
                null, // owningPost
                null, // owningComment
                NotificationType.MESSAGE
        );
        // Envoyer au destinataire
        messagingTemplate.convertAndSendToUser(
                message.getRecipient().getId().toString(),
                "/queue/messages",
                message
        );
        // Envoyer à l'expéditeur pour confirmation
        messagingTemplate.convertAndSendToUser(
                message.getSender().getId().toString(),
                "/queue/messages",
                message
        );
    }
}
