package com.github.farzam_raoufi.template.service;

import com.github.farzam_raoufi.template.dto.auth.AuthResponse;
import com.github.farzam_raoufi.template.dto.auth.LoginDTO;
import com.github.farzam_raoufi.template.dto.auth.RegisterDTO;

public interface AuthService {

    AuthResponse register(RegisterDTO registerDTO);

    AuthResponse login(LoginDTO loginDTO);
}
