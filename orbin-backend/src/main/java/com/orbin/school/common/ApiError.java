package com.orbin.school.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Standard API error response. Never leaks internal implementation details.
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    @Builder.Default
    private final Instant timestamp = Instant.now();

    private final int status;
    private final String code;
    private final String message;
    private final String path;

    /** Field-level validation errors: { "fieldName": ["error1", "error2"] } */
    private final Map<String, List<String>> fieldErrors;
}
