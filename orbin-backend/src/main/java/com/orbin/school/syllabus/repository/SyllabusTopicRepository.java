package com.orbin.school.syllabus.repository;

import com.orbin.school.syllabus.entity.SyllabusTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusTopicRepository extends JpaRepository<SyllabusTopic, Long> {
    List<SyllabusTopic> findBySchoolIdAndChapterIdOrderByDisplayOrderAsc(Long schoolId, Long chapterId);
}
