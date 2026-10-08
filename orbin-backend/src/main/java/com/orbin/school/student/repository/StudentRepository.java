package com.orbin.school.student.repository;

import com.orbin.school.student.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Tenant-scoped student lookup — NEVER fetch without school_id.
     */
    Optional<Student> findByIdAndSchoolId(Long id, Long schoolId);

    Page<Student> findBySchoolId(Long schoolId, Pageable pageable);

    java.util.List<Student> findBySchoolId(Long schoolId);

    Page<Student> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId, Pageable pageable);

    Page<Student> findBySchoolIdAndSectionId(Long schoolId, Long sectionId, Pageable pageable);

    java.util.List<Student> findBySchoolIdAndSectionId(Long schoolId, Long sectionId);

    @Query("SELECT s FROM Student s WHERE s.school.id = :schoolId " +
           "AND (:query IS NULL OR LOWER(s.firstName) LIKE LOWER(CONCAT('%',:query,'%')) " +
           "OR LOWER(s.lastName) LIKE LOWER(CONCAT('%',:query,'%')) " +
           "OR LOWER(s.admissionNumber) LIKE LOWER(CONCAT('%',:query,'%')))")
    Page<Student> searchBySchool(@Param("schoolId") Long schoolId,
                                  @Param("query") String query,
                                  Pageable pageable);

    boolean existsBySchoolIdAndAdmissionNumber(Long schoolId, String admissionNumber);
}
