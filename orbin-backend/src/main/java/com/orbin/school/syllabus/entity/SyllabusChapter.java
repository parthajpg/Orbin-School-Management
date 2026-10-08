package com.orbin.school.syllabus.entity;

import com.orbin.school.academic.entity.Subject;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "syllabus_chapters",
       indexes = @Index(name = "idx_sc_school_subject", columnList = "school_id,subject_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SyllabusChapter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SyllabusTopic> topics = new ArrayList<>();
}
