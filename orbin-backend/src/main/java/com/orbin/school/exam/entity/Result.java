package com.orbin.school.exam.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "results",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "test_id", "student_id"}),
       indexes = {
           @Index(name = "idx_results_school_student", columnList = "school_id,student_id"),
           @Index(name = "idx_results_school_test",    columnList = "school_id,test_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Result extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Test test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "marks_obtained", precision = 6, scale = 2)
    private BigDecimal marksObtained;

    @Column(name = "max_marks", precision = 6, scale = 2)
    private BigDecimal maxMarks;

    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "teacher_note", columnDefinition = "TEXT")
    private String teacherNote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entered_by")
    private User enteredBy;
}
