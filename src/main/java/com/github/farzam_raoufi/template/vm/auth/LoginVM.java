package com.github.farzam_raoufi.template.vm.auth;

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
public class LoginVM {

    @NotBlank(message = "{validation.not_blank}")
    @ValidPattern(regexPattern = RegexPattern.EN_ALPHANUMERIC_REGEX)
    @Size(min = 3, max = 50, message = "{validation.size.username}")
    private String username;

    @NotBlank(message = "{validation.not_blank}")
    @ValidPattern(regexPattern = RegexPattern.PASSWORD_STRONG)
    private String password;
}
