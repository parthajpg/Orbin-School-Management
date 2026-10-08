package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "classes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id","academic_year_id","name"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SchoolClass extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private int displayOrder = 0;
}
