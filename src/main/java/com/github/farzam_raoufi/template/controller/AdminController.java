package com.github.farzam_raoufi.template.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin")
@Tag(name = "Admin", description = "Admin-only endpoints")
public class AdminController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin dashboard", description = "Only accessible by users with ADMIN role")
    public ResponseEntity<Map<String, String>> dashboard() {
        return ResponseEntity.ok(Map.of(
                "message", "Welcome to Admin Dashboard",
                "access", "ADMIN only"
        ));
    }
}
