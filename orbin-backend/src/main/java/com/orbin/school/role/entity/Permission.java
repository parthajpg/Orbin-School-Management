package com.orbin.school.role.entity;

import com.orbin.school.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Fine-grained permission tokens.
 *
 * <p>Examples: students.read, attendance.write, fees.manage, website.edit
 */
@Entity
@Table(name = "permissions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    /** e.g. "students.read", "fees.write", "website.edit" */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 200)
    private String description;

    /** Logical grouping for the UI: "Students", "Fees", "Academic" */
    @Column(length = 60)
    private String module;
}
