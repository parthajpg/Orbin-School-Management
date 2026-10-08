package com.orbin.school.syllabus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateChapterRequest(
    @NotNull Long subjectId,
    Long termId,
    @NotBlank String title,
    String description,
    Integer displayOrder,
    List<String> topicTitles
) {}
