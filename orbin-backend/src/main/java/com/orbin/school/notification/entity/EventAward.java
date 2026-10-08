package com.orbin.school.notification.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_awards")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class EventAward extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, length = 100)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "participant_name", length = 100)
    private String participantName;

    @Column(name = "prize_rank", length = 30)
    private String prizeRank; // FIRST, SECOND, THIRD, SPECIAL_MENTION

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "public", nullable = false)
    @Builder.Default
    private boolean isPublic = true;
}
