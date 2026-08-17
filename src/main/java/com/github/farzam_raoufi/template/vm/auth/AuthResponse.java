package com.github.farzam_raoufi.template.vm.auth;

import com.github.farzam_raoufi.template.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long userId;
    private String username;
    private Role role;
}
