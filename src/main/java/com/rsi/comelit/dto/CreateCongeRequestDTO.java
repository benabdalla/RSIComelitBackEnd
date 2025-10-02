package com.rsi.comelit.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateCongeRequestDTO {
    private Long requesterId;
    private Long validatorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
}
