package com.rsi.comelit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private Long id;
    private UserChatDto sender;
    private UserChatDto recipient;
    private String content;
    private Instant timestamp;
    private boolean read;
}

