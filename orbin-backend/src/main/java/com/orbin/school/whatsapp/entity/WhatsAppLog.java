package com.orbin.school.whatsapp.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "whatsapp_logs",
       indexes = {
           @Index(name = "idx_whatsapp_school_id", columnList = "school_id"),
           @Index(name = "idx_whatsapp_sent_at",   columnList = "school_id,sent_at")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WhatsAppLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 25)
    private String recipientPhone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "student_name", length = 100)
    private String studentName;

    @Column(name = "template_type", nullable = false, length = 50)
    private String templateType;

    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;

    @Column(nullable = false, length = 25)
    @Builder.Default
    private String status = "DELIVERED";

    @Column(name = "meta_message_id", length = 100)
    private String metaMessageId;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private Instant sentAt = Instant.now();
}
