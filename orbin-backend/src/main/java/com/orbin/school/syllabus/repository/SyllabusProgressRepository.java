package com.orbin.school.syllabus.repository;

import com.orbin.school.syllabus.entity.SyllabusProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SyllabusProgressRepository extends JpaRepository<SyllabusProgress, Long> {
    List<SyllabusProgress> findBySchoolIdAndSectionId(Long schoolId, Long sectionId);
    Optional<SyllabusProgress> findBySchoolIdAndChapterIdAndSectionId(Long schoolId, Long chapterId, Long sectionId);
}
