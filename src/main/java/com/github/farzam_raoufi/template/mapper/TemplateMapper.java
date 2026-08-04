package com.github.farzam_raoufi.template.mapper;

import com.github.farzam_raoufi.template.dto.TemplateDTO;
import com.github.farzam_raoufi.template.model.Template;
import com.github.farzam_raoufi.template.vm.TemplateVM;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TemplateMapper {

    TemplateVM toVM(TemplateDTO templateDTO);

    @Mapping(target = "id", ignore = true)
    TemplateDTO toDTO(TemplateVM templateVM);

    TemplateDTO toDTO(Template template);

    Template toEntity(TemplateDTO templateDTO);

    @Mapping(target = "id", ignore = true)
    void updateEntityFromDTO(TemplateDTO dto, @MappingTarget Template entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void partialUpdate(TemplateDTO dto, @MappingTarget Template entity);

}