package com.github.farzam_raoufi.template.service.impl;

import com.github.farzam_raoufi.template.dto.auth.AuthResponse;
import com.github.farzam_raoufi.template.dto.auth.LoginDTO;
import com.github.farzam_raoufi.template.dto.auth.RegisterDTO;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.model.Role;
import com.github.farzam_raoufi.template.model.User;
import com.github.farzam_raoufi.template.repository.UserRepository;
import com.github.farzam_raoufi.template.security.jwt.JwtService;
import com.github.farzam_raoufi.template.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new BusinessException(ErrorMessage.USERNAME_ALREADY_EXISTS, registerDTO.getUsername());
        }

        User user = User.builder()
                .username(registerDTO.getUsername())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .role(Role.USER)
                .enabled(true)
                .build();

        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginDTO loginDTO) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new BusinessException(ErrorMessage.USER_NOT_FOUND, loginDTO.getUsername()));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}
