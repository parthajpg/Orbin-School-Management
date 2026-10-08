package com.orbin.school.exam.repository;

import com.orbin.school.exam.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findBySchoolIdAndTestId(Long schoolId, Long testId);
    List<Result> findBySchoolIdAndStudentId(Long schoolId, Long studentId);
    Optional<Result> findBySchoolIdAndTestIdAndStudentId(Long schoolId, Long testId, Long studentId);
}
