package com.github.farzam_raoufi.template.repository;

import com.github.farzam_raoufi.template.model.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {
    // use paging for findAll
    Page<Template> findAll(Pageable pageable);

    Optional<Template> findById(Long id);

    // Optional: Check existence (more efficient)
    boolean existsById(Long id);

}