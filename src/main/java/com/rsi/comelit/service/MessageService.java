package com.rsi.comelit.service;

import com.rsi.comelit.entity.Message;
import com.rsi.comelit.entity.User;
import com.rsi.comelit.repository.MessageRepository;
import com.rsi.comelit.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public List<Message> getMessagesBetweenUsers(Long userId1, Long userId2) {
        User sender = userRepository.getById(userId1);
        User recipient = userRepository.getById(userId2);
        return messageRepository.findChatMessagesBetweenUsers(sender.getId(), recipient.getId());
    }

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public void markMessagesAsRead(Long senderId, Long recipientId) {
        List<Message> unreadMessages = messageRepository.findUnreadMessages(senderId, recipientId);
        for (Message message : unreadMessages) {
            message.setRead(true);
        }
        messageRepository.saveAll(unreadMessages);
    }
}
