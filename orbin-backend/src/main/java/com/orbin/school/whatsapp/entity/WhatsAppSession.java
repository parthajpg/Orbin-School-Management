package com.orbin.school.whatsapp.entity;

import com.orbin.school.parent.entity.Parent;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Per-phone conversation state for the WhatsApp bot.
 * One row per phone number (upserted on every message).
 */
@Entity
@Table(name = "whatsapp_sessions",
       indexes = @Index(name = "idx_wa_phone", columnList = "phone_number"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WhatsAppSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_student_id")
    private Student selectedStudent;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String state = BotState.IDLE.name();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> context;

    @Column(name = "last_activity", nullable = false)
    @Builder.Default
    private Instant lastActivity = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    public enum BotState {
        IDLE,
        STUDENT_SELECT,
        MAIN_MENU,
        FEE_DETAIL,
        ATTENDANCE_DETAIL,
        HOMEWORK_DETAIL,
        TEST_DETAIL,
        ANNOUNCEMENT_DETAIL,
        ACHIEVEMENT_DETAIL
    }
}
