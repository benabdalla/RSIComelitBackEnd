package com.rsi.comelit.dto;

import com.rsi.comelit.entity.CongeRequestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CongeRequestDTO {
    private Long id;
    private UserSimpleDto requester;
    private UserSimpleDto validator;
    private CongeRequestStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private List<CongeRequestCommentDTO> comments;
}

