package com.github.farzam_raoufi.template.vm.auth;

import com.github.farzam_raoufi.template.validation.ValidationMessages;
import com.github.farzam_raoufi.template.validation.annotation.ValidPattern;
import com.github.farzam_raoufi.template.validation.regex.RegexPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterVM {

    @NotBlank(message = ValidationMessages.NOT_BLANK)
    @ValidPattern(regexPattern = RegexPattern.EN_ALPHANUMERIC_REGEX)
    @Size(min = 3, max = 50, message = ValidationMessages.SIZE_USERNAME)
    private String username;

    @NotBlank(message = ValidationMessages.NOT_BLANK)
    @ValidPattern(regexPattern = RegexPattern.PASSWORD_STRONG)
    private String password;
}
