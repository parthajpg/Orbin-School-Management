package com.orbin.school.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateMaterialRequest(
    @NotNull Long subjectId,
    Long chapterId,
    @NotBlank String title,
    String description,
    String materialType, // PDF, IMAGE, VIDEO_LINK, WORKSHEET, LINK, OTHER
    String fileUrl,
    String externalUrl
) {}
