package com.orbin.school.syllabus.repository;

import com.orbin.school.syllabus.entity.SyllabusChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyllabusChapterRepository extends JpaRepository<SyllabusChapter, Long> {
    List<SyllabusChapter> findBySchoolIdAndSubjectIdOrderByDisplayOrderAsc(Long schoolId, Long subjectId);
    List<SyllabusChapter> findBySchoolIdAndSubjectIdAndTermIdOrderByDisplayOrderAsc(Long schoolId, Long subjectId, Long termId);
    Optional<SyllabusChapter> findByIdAndSchoolId(Long id, Long schoolId);
}
