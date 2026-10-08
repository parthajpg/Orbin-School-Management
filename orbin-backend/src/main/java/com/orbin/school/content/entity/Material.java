package com.orbin.school.content.entity;

import com.orbin.school.academic.entity.Subject;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.syllabus.entity.SyllabusChapter;
import com.orbin.school.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "materials",
       indexes = @Index(name = "idx_materials_school_subject", columnList = "school_id,subject_id"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Material extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private SyllabusChapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_type", nullable = false, length = 30)
    @Builder.Default
    private MaterialType materialType = MaterialType.PDF;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "file_key", length = 300)
    private String fileKey;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @Column(name = "external_url", length = 500)
    private String externalUrl;

    public enum MaterialType { PDF, IMAGE, VIDEO_LINK, WORKSHEET, LINK, OTHER }
}
