package com.orbin.school.parent.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parents",
       indexes = {
           @Index(name = "idx_parents_school_id",    columnList = "school_id"),
           @Index(name = "idx_parents_school_phone", columnList = "school_id,normalized_phone")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Parent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(nullable = false, length = 20)
    private String phone;

    /** Always stored as +91XXXXXXXXXX */
    @Column(name = "normalized_phone", nullable = false, length = 20)
    private String normalizedPhone;

    @Column(length = 150)
    private String email;

    @Column(length = 100)
    private String occupation;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(name = "whatsapp_verified", nullable = false)
    @Builder.Default
    private boolean whatsappVerified = false;

    public String getFullName() {
        return lastName != null ? firstName + " " + lastName : firstName;
    }
}
