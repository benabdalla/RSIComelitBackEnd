package com.rsi.comelit.repository;

import com.rsi.comelit.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender.id = :myId AND m.recipient.id = :otherId) " +
            "OR (m.sender.id = :otherId AND m.recipient.id = :myId) " +
            "ORDER BY m.timestamp ASC")
    List<Message> findChatMessagesBetweenUsers(@Param("myId") Long myId, @Param("otherId") Long otherId);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :senderId AND m.recipient.id = :recipientId AND m.read = false")
    List<Message> findUnreadMessages(@Param("senderId") Long senderId, @Param("recipientId") Long recipientId);
}
