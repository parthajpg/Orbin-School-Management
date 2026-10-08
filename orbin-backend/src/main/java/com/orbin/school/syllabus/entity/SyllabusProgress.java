package com.orbin.school.syllabus.entity;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "syllabus_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "chapter_id", "section_id"}),
       indexes = @Index(name = "idx_sp_school_section", columnList = "school_id,section_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SyllabusProgress extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private SyllabusChapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "planned_start_date")
    private LocalDate plannedStartDate;

    @Column(name = "planned_completion_date")
    private LocalDate plannedCompletionDate;

    @Column(name = "actual_completion_date")
    private LocalDate actualCompletionDate;

    @Column(name = "estimated_periods")
    private Integer estimatedPeriods;

    @Column(name = "actual_periods")
    private Integer actualPeriods;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "revision_date")
    private LocalDate revisionDate;

    public enum ProgressStatus { NOT_STARTED, IN_PROGRESS, COMPLETED, REVISION }
}
