package com.github.farzam_raoufi.template.service;

import com.github.farzam_raoufi.template.dto.auth.AuthDTO;
import com.github.farzam_raoufi.template.dto.auth.LoginDTO;
import com.github.farzam_raoufi.template.dto.auth.RegisterDTO;
import com.github.farzam_raoufi.template.model.AuthProvider;

public interface AuthService {

    AuthDTO register(RegisterDTO registerDTO);

    AuthDTO login(LoginDTO loginDTO);

    AuthDTO loginWithSocial(AuthProvider provider, String token);

    AuthDTO refreshToken(String refreshToken);

    void logout(String refreshToken);
}
