package com.rsi.comelit.dto;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.rsi.comelit.entity.Absence;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

    @Data
    public class AbsenceResponseDto {
        private Long id;
        private Long userId;

        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate date;

        private String status;
        private String justificationText;
        private String justificationFile;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;

        // User info
        private UserDto user;

        @Data
        public static class UserDto {
            private Long id;
            private String firstName;
            private String lastName;
            private String email;
            private String department;
        }

     public static AbsenceResponseDto fromEntity(Absence absence, UserDto user) {
         AbsenceResponseDto dto = new AbsenceResponseDto();
         dto.setId(absence.getId());
         dto.setUserId(absence.getUser() != null ? absence.getUser().getId() : null);
         dto.setDate(absence.getDate());
         dto.setStatus(absence.getStatus().getDisplayName());
         dto.setJustificationText(absence.getJustificationText());
         dto.setJustificationFile(absence.getJustificationFile());
         dto.setCreatedAt(absence.getCreatedAt());
         dto.setUpdatedAt(absence.getUpdatedAt());
         dto.setUser(user);
         return dto;
     }
    }

