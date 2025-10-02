package com.rsi.comelit.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbsencePageResponseDto {
    private List<AbsenceResponseDto> absences;
    private long total;
    private int page;
    private int limit;
    private int totalPages;
}