package com.orbin.school.exam.entity;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.entity.Subject;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tests",
       indexes = @Index(name = "idx_tests_school_section", columnList = "school_id,section_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Test extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "test_date")
    private LocalDate testDate;

    @Column(name = "duration_min")
    private Integer durationMin;

    @Column(name = "max_marks", precision = 6, scale = 2)
    private BigDecimal maxMarks;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TestStatus status = TestStatus.DRAFT;

    public enum TestStatus { DRAFT, SCHEDULED, COMPLETED }
}
