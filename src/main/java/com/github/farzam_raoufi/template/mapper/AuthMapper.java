package com.github.farzam_raoufi.template.mapper;

import com.github.farzam_raoufi.template.dto.auth.AuthDTO;
import com.github.farzam_raoufi.template.vm.auth.AuthVM;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AuthMapper {

    AuthVM toVM(AuthDTO authDTO);

    AuthDTO toDTO(AuthVM authVM);
}