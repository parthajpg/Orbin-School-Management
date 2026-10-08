package com.orbin.school.whatsapp.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SendWhatsAppRequest {

    private String templateType;

    private List<RecipientItem> recipients;

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class RecipientItem {
        private Long studentId;
        private String studentName;
        @NotBlank
        private String parentName;
        @NotBlank
        private String parentPhone;
        private String customMessage;
    }
}
