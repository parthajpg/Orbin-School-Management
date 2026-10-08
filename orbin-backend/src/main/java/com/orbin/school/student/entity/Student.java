package com.orbin.school.student.entity;

import com.orbin.school.academic.entity.AcademicYear;
import com.orbin.school.academic.entity.Section;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "students",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id","admission_number"}),
       indexes = {
           @Index(name = "idx_students_school_id",      columnList = "school_id"),
           @Index(name = "idx_students_school_section", columnList = "school_id,section_id"),
           @Index(name = "idx_students_school_ay",      columnList = "school_id,academic_year_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Student extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "admission_number", nullable = false, length = 30)
    private String admissionNumber;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 10)
    private String gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    public String getFullName() {
        return lastName != null ? firstName + " " + lastName : firstName;
    }

    public enum StudentStatus { ACTIVE, TRANSFERRED, ARCHIVED }
}
