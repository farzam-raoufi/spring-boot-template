package com.github.farzam_raoufi.template.vm.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.farzam_raoufi.template.model.ConfigValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SystemConfigVM {
    private String key;
    private String value;
    private String defaultValue;
    private ConfigValueType type;
    private boolean sensitive;
    private boolean overridden;
    private String description;
    private Instant updatedAt;
    private String updatedBy;
}