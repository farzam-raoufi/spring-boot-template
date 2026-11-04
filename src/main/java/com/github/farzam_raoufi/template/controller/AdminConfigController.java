package com.github.farzam_raoufi.template.controller;

import com.github.farzam_raoufi.template.mapper.SystemConfigMapper;
import com.github.farzam_raoufi.template.service.SystemConfigService;
import com.github.farzam_raoufi.template.vm.config.SystemConfigVM;
import com.github.farzam_raoufi.template.vm.config.UpdateSystemConfigVM;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Config", description = "Runtime configuration management (ADMIN only)")
public class AdminConfigController {

    private final SystemConfigService configService;
    private final SystemConfigMapper configMapper;

    @GetMapping
    @Operation(summary = "List all configs", description = "Sensitive values are masked")
    public ResponseEntity<List<SystemConfigVM>> getAll() {
        return ResponseEntity.ok(configMapper.toVMList(configService.getAll()));
    }

    @GetMapping("/{key}")
    @Operation(summary = "Get one config by key")
    public ResponseEntity<SystemConfigVM> getByKey(@PathVariable String key) {
        return ResponseEntity.ok(configMapper.toVM(configService.getByKey(key)));
    }

    @PutMapping("/{key}")
    @Operation(summary = "Set a config value")
    public ResponseEntity<SystemConfigVM> update(@PathVariable String key,
                                                 @Valid @RequestBody UpdateSystemConfigVM body) {
        return ResponseEntity.ok(configMapper.toVM(configService.updateValue(key, body.getValue())));
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "Reset a config to its default value")
    public ResponseEntity<SystemConfigVM> reset(@PathVariable String key) {
        return ResponseEntity.ok(configMapper.toVM(configService.resetToDefault(key)));
    }
}