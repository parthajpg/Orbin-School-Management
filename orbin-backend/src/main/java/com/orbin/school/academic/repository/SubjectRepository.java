package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findBySchoolId(Long schoolId);
    List<Subject> findBySchoolIdAndSchoolClassId(Long schoolId, Long classId);
    Optional<Subject> findByIdAndSchoolId(Long id, Long schoolId);
}
