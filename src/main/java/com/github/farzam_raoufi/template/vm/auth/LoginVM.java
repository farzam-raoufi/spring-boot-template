package com.github.farzam_raoufi.template.vm.auth;

import jakarta.validation.constraints.NotBlank;
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
    private String username;

    @NotBlank(message = "{validation.not_blank}")
    private String password;
}
