package com.rsi.comelit.service;

import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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
    public void sendNotificationCount(User user) {
        if (user == null || user.getId() == null) {
            return;
        }

        try {
            // Récupérer le nombre de notifications non vues
            int unseenCount = notificationRepository.countByReceiverAndIsSeenIsFalse(user);

            // Créer l'objet à envoyer
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("count", unseenCount);
            notificationData.put("userId", user.getId());
            notificationData.put("timestamp", System.currentTimeMillis());

            // Envoyer via WebSocket à l'utilisateur spécifique
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications/count",
                    notificationData
            );

        } catch (Exception e) {
            // Log l'erreur mais ne pas interrompre le processus
            System.err.println("Erreur lors de l'envoi du nombre de notifications pour l'utilisateur " + user.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Envoie le nombre de notifications à plusieurs utilisateurs
     *
     * @param users Liste des utilisateurs
     */
    public void sendNotificationCountToUsers(User... users) {
        if (users != null) {
            for (User user : users) {
                sendNotificationCount(user);
            }
        }
    }
}
