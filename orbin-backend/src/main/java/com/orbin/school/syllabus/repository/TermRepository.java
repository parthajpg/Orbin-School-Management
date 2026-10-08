package com.orbin.school.syllabus.repository;

import com.orbin.school.syllabus.entity.Term;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TermRepository extends JpaRepository<Term, Long> {
    List<Term> findBySchoolIdOrderByDisplayOrderAsc(Long schoolId);
    List<Term> findBySchoolIdAndAcademicYearIdOrderByDisplayOrderAsc(Long schoolId, Long academicYearId);
    Optional<Term> findByIdAndSchoolId(Long id, Long schoolId);
}
