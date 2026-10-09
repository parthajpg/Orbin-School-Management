package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "timetable_entries",
       uniqueConstraints = @UniqueConstraint(
               columnNames = {"school_id", "academic_year_id", "section_id", "period_slot_id", "day_of_week"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class TimetableEntry extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "period_slot_id", nullable = false)
    private PeriodSlot periodSlot;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek; // 1 = Monday, ..., 6 = Saturday

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject; // Nullable for Nursery / Homeroom Primary classes

    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @Column(name = "is_substitution", nullable = false)
    @Builder.Default
    private Boolean isSubstitution = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_teacher_id")
    private User originalTeacher;
}
