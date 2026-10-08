package com.orbin.school.whatsapp.dto;

import lombok.*;

import java.time.Instant;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WhatsAppNotificationDto {
    private Long id;
    private String recipientName;
    private String recipientPhone;
    private Long studentId;
    private String studentName;
    private String templateType;
    private String messageContent;
    private String status;
    private String metaMessageId;
    private Instant sentAt;
}
