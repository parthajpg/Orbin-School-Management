package com.orbin.school.parent.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_students",
       uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id","student_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class ParentStudent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 30)
    private String relationship;  // FATHER, MOTHER, GUARDIAN

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Column(name = "can_receive_notifications", nullable = false)
    @Builder.Default
    private boolean canReceiveNotifications = true;
}
