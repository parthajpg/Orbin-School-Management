package com.orbin.school.school.entity;

import com.orbin.school.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents one school tenant in the platform.
 * Every school-owned entity has a FK back to this.
 */
@Entity
@Table(name = "schools")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class School extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "short_name", length = 30)
    private String shortName;

    /** Subdomain slug: e.g. "greenfield" → greenfield.orbinschool.in */
    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(name = "custom_domain", length = 100)
    private String customDomain;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 20)
    private String pincode;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SchoolStatus status = SchoolStatus.ACTIVE;

    @OneToOne(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SchoolBranding branding;

    public enum SchoolStatus {
        ACTIVE, SUSPENDED, PENDING_SETUP, ARCHIVED
    }
}
