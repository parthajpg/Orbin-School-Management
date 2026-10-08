package com.orbin.school.exam.repository;

import com.orbin.school.exam.entity.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestRepository extends JpaRepository<Test, Long> {
    Page<Test> findBySchoolIdAndSectionIdOrderByTestDateDesc(Long schoolId, Long sectionId, Pageable pageable);
    Optional<Test> findByIdAndSchoolId(Long id, Long schoolId);
}
