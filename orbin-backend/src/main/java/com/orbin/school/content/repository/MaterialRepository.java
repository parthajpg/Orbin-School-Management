package com.orbin.school.content.repository;

import com.orbin.school.content.entity.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Page<Material> findBySchoolIdAndSubjectIdOrderByCreatedAtDesc(Long schoolId, Long subjectId, Pageable pageable);
    Optional<Material> findByIdAndSchoolId(Long id, Long schoolId);
}
