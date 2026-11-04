package com.github.farzam_raoufi.template.vm.config;

import com.github.farzam_raoufi.template.config.KeepRaw;
import com.github.farzam_raoufi.template.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSystemConfigVM {

    @NotBlank(message = ValidationMessages.NOT_BLANK)
    @Size(max = 4000, message = ValidationMessages.SIZE_SYSTEM_CONFIG_VALUE)
    @KeepRaw
    private String value;
}