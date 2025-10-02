package com.rsi.comelit.dto;

import com.rsi.comelit.annotation.ValidEmail;
import com.rsi.comelit.annotation.ValidPassword;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDto {
    @ValidEmail
    private String email;

    @ValidPassword
    private String password;
}
