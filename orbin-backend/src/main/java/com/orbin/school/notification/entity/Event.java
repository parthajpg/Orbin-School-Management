package com.orbin.school.notification.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "events",
       indexes = @Index(name = "idx_events_school_date", columnList = "school_id,event_date"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Event extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    @Builder.Default
    private EventType eventType = EventType.GENERAL;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 200)
    private String venue;

    @Column(name = "public", nullable = false)
    @Builder.Default
    private boolean isPublic = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EventStatus status = EventStatus.UPCOMING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum EventType { ANNUAL_DAY, SPORTS, CULTURAL, PTM, GENERAL }
    public enum EventStatus { UPCOMING, ONGOING, COMPLETED, CANCELLED }
}
