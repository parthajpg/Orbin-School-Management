package com.orbin.school.academic.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"class_id", "name"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Section extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private SchoolClass schoolClass;

    @Column(nullable = false, length = 10)
    private String name;   // "A", "B", "C"

    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher;
}
