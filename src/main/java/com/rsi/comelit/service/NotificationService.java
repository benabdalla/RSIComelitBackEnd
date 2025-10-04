package com.rsi.comelit.service;

import com.rsi.comelit.entity.Comment;
import com.rsi.comelit.entity.Notification;
import com.rsi.comelit.entity.Post;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.enumeration.NotificationType;

import java.util.List;

public interface NotificationService {
    Notification getNotificationById(Long notificationId);

    Notification getNotificationByReceiverAndOwningPostAndType(User receiver, Post owningPost, NotificationType type);

    void sendNotification(User receiver, User sender, Post owningPost, Comment owningComment, NotificationType type);

    void removeNotification(User receiver, Post owningPost, NotificationType type);
    List<Notification> getNotificationsForAuthUserPaginate(Integer page, Integer size);
    void markAllSeen();
    void markAllRead();

    void deleteNotification(User receiver, Post owningPost, NotificationType type);
    void deleteNotificationByOwningPost(Post owningPost);
    void deleteNotificationByOwningComment(Comment owningComment);

    int getUnseenNotificationCount(User user);
}
