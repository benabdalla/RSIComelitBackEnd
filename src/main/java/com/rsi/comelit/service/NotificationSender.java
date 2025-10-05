package com.rsi.comelit.service;

import com.rsi.comelit.dto.NotificationDto;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSender {
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;

    /**
     * Envoie le nombre de notifications non vues à un utilisateur spécifique
     *
     * @param user L'utilisateur à qui envoyer le nombre
     */
    public void sendNotificationCount(User user, NotificationDto notification) {
        if (user == null || user.getId() == null) {
            return;
        }
        try {
            // Envoyer via WebSocket à l'utilisateur spécifique
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications/count",
                    notification
            );

        } catch (Exception e) {
            // Log l'erreur mais ne pas interrompre le processus
            System.err.println("Erreur lors de l'envoi du nombre de notifications pour l'utilisateur " + user.getId() + ": " + e.getMessage());
        }
    }

}
