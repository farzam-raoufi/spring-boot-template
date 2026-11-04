package com.github.farzam_raoufi.template.dto.config;

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
public class SystemConfigDTO {
    private String key;
    private String value;          // masked when sensitive
    private String defaultValue;   // masked when sensitive
    private ConfigValueType type;
    private boolean sensitive;
    private boolean overridden;    // true = admin changed it, false = default is in use
    private String description;
    private Instant updatedAt;
    private String updatedBy;
}