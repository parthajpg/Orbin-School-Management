package com.orbin.school.notification.entity;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "announcements",
       indexes = {
           @Index(name = "idx_announcements_school",   columnList = "school_id,publish_at"),
           @Index(name = "idx_announcements_audience", columnList = "school_id,audience")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Announcement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Audience audience = Audience.ALL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private Section section;

    @Column(nullable = false)
    @Builder.Default
    private boolean urgent = false;

    @Column(name = "publish_at", nullable = false)
    @Builder.Default
    private Instant publishAt = Instant.now();

    @Column(name = "expires_at")
    private Instant expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum Audience { ALL, STAFF, PARENTS, CLASS_SPECIFIC, PUBLIC }
}
