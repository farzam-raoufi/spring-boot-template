package com.github.farzam_raoufi.template.mapper;

import com.github.farzam_raoufi.template.dto.config.SystemConfigDTO;
import com.github.farzam_raoufi.template.vm.config.SystemConfigVM;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SystemConfigMapper {

    SystemConfigVM toVM(SystemConfigDTO dto);

    List<SystemConfigVM> toVMList(List<SystemConfigDTO> dtos);
}