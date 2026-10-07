package com.github.farzam_raoufi.template.vm.auth;

import com.github.farzam_raoufi.template.validation.ValidationMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginVM {

    @NotBlank(message = ValidationMessages.NOT_BLANK)
    private String token;
}
