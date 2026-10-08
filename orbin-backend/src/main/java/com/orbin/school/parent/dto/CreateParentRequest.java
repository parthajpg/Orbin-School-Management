package com.orbin.school.parent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateParentRequest(
    @NotBlank @Size(max = 80) String firstName,
    @Size(max = 80) String lastName,
    @NotBlank @Size(max = 20) String phone,
    String email,
    String occupation,
    String address
) {}
