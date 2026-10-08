package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "subjects",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "code"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "subject_type", nullable = false, length = 30)
    @Builder.Default
    private SubjectType subjectType = SubjectType.ACADEMIC;

    public enum SubjectType { ACADEMIC, ACTIVITY, LANGUAGE }
}
