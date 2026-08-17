package com.github.farzam_raoufi.template.mapper;

import com.github.farzam_raoufi.template.dto.auth.RegisterDTO;
import com.github.farzam_raoufi.template.vm.auth.RegisterVM;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegisterMapper {

    RegisterVM toVM(RegisterDTO RegisterDTO);

    RegisterDTO toDTO(RegisterVM RegisterVM);
}