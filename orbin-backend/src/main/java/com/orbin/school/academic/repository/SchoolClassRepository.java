package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.SchoolClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long> {
    List<SchoolClass> findBySchoolIdOrderByDisplayOrderAsc(Long schoolId);
    List<SchoolClass> findBySchoolIdAndAcademicYearIdOrderByDisplayOrderAsc(Long schoolId, Long academicYearId);
    Optional<SchoolClass> findByIdAndSchoolId(Long id, Long schoolId);
}
