package com.orbin.school.school.entity;

import com.orbin.school.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "school_modules",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "module_name"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SchoolModule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "module_name", nullable = false, length = 60)
    private String moduleName;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    @Column(columnDefinition = "jsonb")
    private String config;
}
