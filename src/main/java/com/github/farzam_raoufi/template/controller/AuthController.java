package com.github.farzam_raoufi.template.controller;

import com.github.farzam_raoufi.template.dto.auth.AuthResponse;
import com.github.farzam_raoufi.template.mapper.LoginMapper;
import com.github.farzam_raoufi.template.mapper.RegisterMapper;
import com.github.farzam_raoufi.template.service.AuthService;
import com.github.farzam_raoufi.template.vm.auth.LoginVM;
import com.github.farzam_raoufi.template.vm.auth.RegisterVM;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and Login APIs")
public class AuthController {

    private final AuthService authService;
    private final RegisterMapper registerMapper;
    private final LoginMapper loginMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user with USER role and returns JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterVM registerVM) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerMapper.toDTO(registerVM)));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginVM loginVM) {
        return ResponseEntity.ok(authService.login(loginMapper.toDTO(loginVM)));
    }
}
