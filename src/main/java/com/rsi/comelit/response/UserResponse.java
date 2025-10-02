package com.rsi.comelit.response;

import com.rsi.comelit.entity.User;
import lombok.*;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private User user;
    private Boolean followedByAuthUser;
}
