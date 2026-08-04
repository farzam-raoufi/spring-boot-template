package com.github.farzam_raoufi.template.service;

import com.github.farzam_raoufi.template.dto.TemplateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TemplateService {

    Page<TemplateDTO> getAllTemplateDTOPage(Pageable pageable);

    TemplateDTO getTemplateById(Long id);

    TemplateDTO createTemplate(TemplateDTO templateDTO);

    TemplateDTO updateTemplate(Long id, TemplateDTO templateDTO);

    TemplateDTO patchTemplate(Long id, TemplateDTO templateDTO);

    void deleteTemplate(Long id);
}
