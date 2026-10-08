package com.orbin.school.fee.repository;

import com.orbin.school.fee.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findBySchoolId(Long schoolId);
    List<FeeStructure> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    Optional<FeeStructure> findByIdAndSchoolId(Long id, Long schoolId);
}
