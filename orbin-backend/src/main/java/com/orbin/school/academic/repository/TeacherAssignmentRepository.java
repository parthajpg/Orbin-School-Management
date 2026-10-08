package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.TeacherAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
    List<TeacherAssignment> findBySchoolId(Long schoolId);
    List<TeacherAssignment> findBySchoolIdAndAcademicYearId(Long schoolId, Long academicYearId);
    List<TeacherAssignment> findBySchoolIdAndTeacherId(Long schoolId, Long teacherId);
    List<TeacherAssignment> findBySchoolIdAndSectionId(Long schoolId, Long sectionId);
}
