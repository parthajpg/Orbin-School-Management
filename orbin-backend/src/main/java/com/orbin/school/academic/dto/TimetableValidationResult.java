package com.orbin.school.academic.dto;

import java.util.List;

public record TimetableValidationResult(
    int totalRows,
    int validRows,
    int errorRows,
    List<String> errors,
    List<TimetableEntryDto> previewEntries,
    boolean canCommit
) {}
