package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "academic_years",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "name"}),
       indexes = @Index(name = "idx_ay_school_id", columnList = "school_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AcademicYear extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 20)
    private String name;   // e.g. "2024-25"

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "is_current", nullable = false)
    @Builder.Default
    private boolean current = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.ACTIVE;

    public enum Status { ACTIVE, ARCHIVED }
}
