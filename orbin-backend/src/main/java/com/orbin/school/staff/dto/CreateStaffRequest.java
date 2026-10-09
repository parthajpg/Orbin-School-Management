package com.orbin.school.staff.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class CreateStaffRequest {

    private String employeeId;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String role;
    private String designation;
    private String department;
    private Long assignedClassId;
    private Long assignedSectionId;
    private Boolean isHomeroom;
    private List<Long> assignedSubjectIds;
    private List<String> subjectsTaught;
    private String qualification;
    private LocalDate dateOfJoining;
    private String initialPassword;
}
