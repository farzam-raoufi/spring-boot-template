package com.github.farzam_raoufi.template.dto.auth;

import com.github.farzam_raoufi.template.model.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserInfo {

    private AuthProvider provider;
    private String providerId;
    private String email;
    private String name;
}