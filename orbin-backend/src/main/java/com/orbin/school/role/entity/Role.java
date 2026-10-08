package com.orbin.school.role.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * A role within a school (or platform-level if school is null).
 *
 * <p>Built-in role names: ORBIN_ADMIN, PRINCIPAL, OFFICE_ADMIN,
 * ACADEMIC_AUTHORITY, TEACHER, PARENT.
 *
 * <p>School principals can create custom roles within their school.
 */
@Entity
@Table(name = "roles",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "name"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Role extends BaseEntity {

    /** Null for platform-level roles (e.g. ORBIN_ADMIN). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(nullable = false, length = 60)
    private String name;

    @Column(length = 200)
    private String description;

    @Column(name = "is_system_role", nullable = false)
    @Builder.Default
    private boolean systemRole = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
