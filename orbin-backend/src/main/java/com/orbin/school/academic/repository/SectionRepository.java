package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    Optional<Section> findByIdAndSchoolId(Long id, Long schoolId);
    List<Section> findBySchoolId(Long schoolId);
    List<Section> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);
}
