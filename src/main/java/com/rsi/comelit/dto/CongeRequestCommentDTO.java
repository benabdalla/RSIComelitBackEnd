package com.rsi.comelit.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CongeRequestCommentDTO {
    private Long id;
    private Long authorId;
    private String comment;
    private LocalDateTime createdAt;
}

