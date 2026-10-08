package com.orbin.school.attendance.entity;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "attendance",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id","student_id","date"}),
       indexes = {
           @Index(name = "idx_attendance_school_date",    columnList = "school_id,date"),
           @Index(name = "idx_attendance_school_student", columnList = "school_id,student_id"),
           @Index(name = "idx_attendance_section_date",   columnList = "section_id,date")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marked_by")
    private User markedBy;

    @Column(length = 300)
    private String notes;

    public enum AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }
}
