package com.github.farzam_raoufi.template.dto.auth;

import com.github.farzam_raoufi.template.model.User;

public record RefreshTokenDTO(User user, String rawToken) {
}