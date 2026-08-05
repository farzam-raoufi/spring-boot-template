package com.github.farzam_raoufi.template.controller;

import com.github.farzam_raoufi.template.mapper.TemplateMapper;
import com.github.farzam_raoufi.template.service.TemplateService;
import com.github.farzam_raoufi.template.validation.groups.Create;
import com.github.farzam_raoufi.template.vm.TemplateVM;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("template")
@RequiredArgsConstructor
@Tag(name = "Template", description = "APIs for template")
public class TemplateController {

    final private TemplateMapper templateMapper;
    final private TemplateService templateService;

    @GetMapping("/{id}")
    @Operation(summary = "Get template by id", description = "Returns a template by id")
    public ResponseEntity<TemplateVM> template(@PathVariable Long id) {
        return ResponseEntity.ok(
                templateMapper.toVM(
                        templateService.getTemplateById(id)
                )
        );
    }


    @GetMapping
    @Operation(summary = "Get all template", description = "Returns list of all templates with pagination")
    public ResponseEntity<Page<TemplateVM>> templates(
            @ParameterObject @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                templateService.getAllTemplateDTOPage(pageable)
                        .map(templateMapper::toVM)
        );
    }

    @PostMapping
    @Operation(summary = "Create a new template")
    public ResponseEntity<TemplateVM> createTemplate(@Validated(Create.class) @RequestBody TemplateVM templateVM) { // validation
        return ResponseEntity.status(HttpStatus.CREATED).body(templateMapper.toVM(
                templateService.createTemplate(
                        templateMapper.toDTO(templateVM)
                )
        ));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update template by id")
    public ResponseEntity<TemplateVM> patchTemplate(@PathVariable Long id, @Validated(Create.class) @RequestBody TemplateVM templateVM) { // validation
        return ResponseEntity.ok(
                templateMapper.toVM(
                        templateService.patchTemplate(
                                id,
                                templateMapper.toDTO(templateVM)
                        )
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template by id")
    public ResponseEntity<TemplateVM> updateTemplate(@PathVariable Long id, @Validated(Create.class) @RequestBody TemplateVM templateVM) { // validation
        return ResponseEntity.ok(
                templateMapper.toVM(
                        templateService.updateTemplate(
                                id,
                                templateMapper.toDTO(templateVM)
                        )
                )
        );
    }
}