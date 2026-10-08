package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findBySchoolId(Long schoolId);
    Optional<AcademicYear> findByIdAndSchoolId(Long id, Long schoolId);
    Optional<AcademicYear> findBySchoolIdAndCurrentTrue(Long schoolId);
}
