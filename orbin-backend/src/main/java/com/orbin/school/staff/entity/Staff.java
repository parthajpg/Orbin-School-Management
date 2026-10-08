package com.orbin.school.staff.entity;

import com.orbin.school.academic.entity.SchoolClass;
import com.orbin.school.academic.entity.Section;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staff",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"school_id", "employee_id"}),
           @UniqueConstraint(columnNames = {"school_id", "email"})
       },
       indexes = {
           @Index(name = "idx_staff_school_id", columnList = "school_id"),
           @Index(name = "idx_staff_user_id", columnList = "user_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Staff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "employee_id", nullable = false, length = 40)
    private String employeeId;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 25)
    private String phone;

    @Column(nullable = false, length = 40)
    @Builder.Default
    private String role = "TEACHER";

    @Column(length = 100)
    private String designation;

    @Column(length = 100)
    private String department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_class_id")
    private SchoolClass assignedClass;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_section_id")
    private Section assignedSection;

    @Column(length = 150)
    private String qualification;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String status = "ACTIVE";
}
