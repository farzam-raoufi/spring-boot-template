package com.github.farzam_raoufi.template.service.impl;

import com.github.farzam_raoufi.template.dto.TemplateDTO;
import com.github.farzam_raoufi.template.mapper.TemplateMapper;
import com.github.farzam_raoufi.template.model.Template;
import com.github.farzam_raoufi.template.repository.TemplateRepository;
import com.github.farzam_raoufi.template.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
//این آنتوتیشن برای مدیریت تراکنش‌های پایگاه‌داده استفاده می‌شود. اگر متد بدون خطا تمام شود → تراکنش را commit می‌کند و اگر exception رخ دهد → تراکنش را rollback می‌کند
public class TemplateServiceImp implements TemplateService {
    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    @Override
    @Transactional(readOnly = true) // this is necessary for performance
    public Page<TemplateDTO> getAllTemplateDTOPage(Pageable pageable) {
        return templateRepository.findAll(pageable).map(templateMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true) // this is necessary for performance
    public TemplateDTO getTemplateById(Long id) {
        Template template = templateRepository.findById(id).orElseThrow(); // add Exception
        return templateMapper.toDTO(template);
    }

    @Override
    public TemplateDTO createTemplate(TemplateDTO templateDTO) {
        return templateMapper.toDTO(templateRepository.save(templateMapper.toEntity(templateDTO)));
    }

    @Override
    public TemplateDTO updateTemplate(Long id, TemplateDTO templateDTO) {

        Template existing = templateRepository.findById(id).orElseThrow(); // add Exception

        templateMapper.updateEntityFromDTO(templateDTO, existing);
        return templateMapper.toDTO(templateRepository.save(existing));

    }

    @Override
    public TemplateDTO patchTemplate(Long id, TemplateDTO templateDTO) {
        Template existing = templateRepository.findById(id).orElseThrow(); // add Exception
        templateMapper.partialUpdate(templateDTO, existing);
        return templateMapper.toDTO(templateRepository.save(existing));
    }

    @Override
    public void deleteTemplate(Long id) {
        if (!templateRepository.existsById(id)) {
            throw new RuntimeException(); // add Exception
        }
        templateRepository.deleteById(id);
    }

}
