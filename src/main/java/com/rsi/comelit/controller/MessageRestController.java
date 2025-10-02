package com.rsi.comelit.controller;

import com.rsi.comelit.dto.MessageDto;
import com.rsi.comelit.mapper.MessageMapper;
import com.rsi.comelit.service.MessageService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/messages")
@AllArgsConstructor
public class MessageRestController {
    private final MessageService messageService;
    private final MessageMapper messageMapper;

    @GetMapping("/{userId1}/{userId2}")
    public List<MessageDto> getMessagesBetweenUsers(@PathVariable Long userId1, @PathVariable Long userId2) {
        return messageService.getMessagesBetweenUsers(userId1, userId2)
                .stream()
                .map(messageMapper::toDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/read")
    public void markMessagesAsRead(@RequestParam Long senderId, @RequestParam Long recipientId) {
        messageService.markMessagesAsRead(senderId, recipientId);
    }

}
