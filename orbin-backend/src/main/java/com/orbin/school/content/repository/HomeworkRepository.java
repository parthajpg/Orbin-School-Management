package com.orbin.school.content.repository;

import com.orbin.school.content.entity.Homework;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    Page<Homework> findBySchoolIdAndSectionIdOrderByDueDateDesc(Long schoolId, Long sectionId, Pageable pageable);
    List<Homework> findBySchoolIdAndSectionIdAndDueDate(Long schoolId, Long sectionId, LocalDate dueDate);
    Optional<Homework> findByIdAndSchoolId(Long id, Long schoolId);
}
