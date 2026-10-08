package com.orbin.school.staff.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StaffDto {
    private Long id;
    private String employeeId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String designation;
    private String department;
    private Long assignedClassId;
    private String assignedClassName;
    private Long assignedSectionId;
    private String assignedSectionName;
    private List<String> subjectsTaught;
    private String qualification;
    private LocalDate dateOfJoining;
    private String status;
}
