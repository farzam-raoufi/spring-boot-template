package com.github.farzam_raoufi.template.vm;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.farzam_raoufi.template.config.KeepRaw;
import com.github.farzam_raoufi.template.validation.annotation.ValidPattern;
import com.github.farzam_raoufi.template.validation.groups.Create;
import com.github.farzam_raoufi.template.validation.groups.Update;
import com.github.farzam_raoufi.template.validation.regex.RegexPattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TemplateVM {
    private Long id;
    @NotNull(message = "Name is required", groups = {Create.class})
    @NotBlank(message = "Name is required", groups = {Create.class})
//    @ValidPattern(regexPattern = RegexPattern.NOT_BLANK, groups = {Create.class, Update.class}) // <--- Using custom regex pattern
    @Size(min = 1, max = 100, groups = {Create.class, Update.class})
    @ValidPattern(regexPattern = RegexPattern.ALPHANUMERIC_REGEX, groups = {Create.class, Update.class}) // <--- Using custom regex pattern
    @KeepRaw // <--- This field will bypass the global trim logic
    private String name;
}
