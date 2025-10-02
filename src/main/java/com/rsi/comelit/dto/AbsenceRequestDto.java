package com.rsi.comelit.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;


import lombok.Data;
import lombok.ToString;

import java.time.LocalDate;

@Data
@ToString
public class AbsenceRequestDto {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    // These are optional and can be null
    private String justificationText;

    private String justificationFile;
}


