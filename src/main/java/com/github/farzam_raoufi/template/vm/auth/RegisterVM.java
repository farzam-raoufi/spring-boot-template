package com.github.farzam_raoufi.template.vm.auth;

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

    @NotBlank(message = "{validation.not_blank}")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "{validation.not_blank}")
    @Size(min = 8, max = 100)
    private String password;
}
