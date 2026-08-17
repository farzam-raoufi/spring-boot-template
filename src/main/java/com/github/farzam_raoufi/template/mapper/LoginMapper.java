package com.github.farzam_raoufi.template.mapper;

import com.github.farzam_raoufi.template.dto.auth.LoginDTO;
import com.github.farzam_raoufi.template.vm.auth.LoginVM;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginMapper {

    LoginVM toVM(LoginDTO LoginDTO);

    LoginDTO toDTO(LoginVM LoginVM);
}