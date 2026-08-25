package com.github.farzam_raoufi.template.controller;

import com.github.farzam_raoufi.template.mapper.AuthMapper;
import com.github.farzam_raoufi.template.mapper.LoginMapper;
import com.github.farzam_raoufi.template.mapper.RegisterMapper;
import com.github.farzam_raoufi.template.service.AuthService;
import com.github.farzam_raoufi.template.vm.auth.AuthVM;
import com.github.farzam_raoufi.template.vm.auth.LoginVM;
import com.github.farzam_raoufi.template.vm.auth.RefreshTokenVM;
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
    private final AuthMapper authMapper;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user with USER role and returns JWT")
    public ResponseEntity<AuthVM> register(@Valid @RequestBody RegisterVM registerVM) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authMapper.toVM(
                authService.register(registerMapper.toDTO(registerVM)))
        );
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates user and returns JWT")
    public ResponseEntity<AuthVM> login(@Valid @RequestBody LoginVM loginVM) {
        return ResponseEntity.ok(authMapper.toVM(authService.login(loginMapper.toDTO(loginVM)))
        );
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Exchanges a valid refresh token for a new access token and refresh token")
    public ResponseEntity<AuthVM> refresh(@Valid @RequestBody RefreshTokenVM refreshTokenVM) {
        return ResponseEntity.ok(authMapper.toVM(
                authService.refreshToken(refreshTokenVM.getRefreshToken()))
        );
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Revokes the given refresh token")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenVM refreshTokenVM) {
        authService.logout(refreshTokenVM.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
