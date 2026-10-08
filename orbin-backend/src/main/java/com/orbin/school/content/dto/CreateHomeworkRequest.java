package com.orbin.school.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateHomeworkRequest(
    @NotNull Long sectionId,
    @NotNull Long subjectId,
    @NotBlank String title,
    String description,
    @NotNull LocalDate dueDate,
    String attachmentUrl
) {}
