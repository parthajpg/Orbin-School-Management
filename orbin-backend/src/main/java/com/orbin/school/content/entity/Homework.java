package com.orbin.school.content.entity;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.entity.Subject;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "homework",
       indexes = {
           @Index(name = "idx_hw_school_section", columnList = "school_id,section_id"),
           @Index(name = "idx_hw_due_date",       columnList = "school_id,due_date")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Homework extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;

    @Column(name = "attachment_key", length = 300)
    private String attachmentKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private HomeworkStatus status = HomeworkStatus.PUBLISHED;

    @Column(name = "publish_at")
    private Instant publishAt;

    public enum HomeworkStatus { DRAFT, PUBLISHED, SCHEDULED }
}
