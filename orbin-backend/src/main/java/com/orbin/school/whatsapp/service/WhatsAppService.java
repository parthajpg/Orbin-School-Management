package com.orbin.school.whatsapp.service;

import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.student.entity.Student;
import com.orbin.school.student.repository.StudentRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.whatsapp.dto.SendWhatsAppRequest;
import com.orbin.school.whatsapp.dto.WhatsAppNotificationDto;
import com.orbin.school.whatsapp.entity.WhatsAppLog;
import com.orbin.school.whatsapp.repository.WhatsAppLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WhatsAppService {

    private final WhatsAppLogRepository whatsAppLogRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<WhatsAppNotificationDto> getRecentLogs() {
        Long schoolId = TenantContext.requireSchoolId();
        return whatsAppLogRepository.findTop50BySchoolIdOrderBySentAtDesc(schoolId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<WhatsAppNotificationDto> sendAbsenceBlast(SendWhatsAppRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));

        List<WhatsAppNotificationDto> delivered = new ArrayList<>();
        String todayStr = LocalDate.now().toString();

        for (SendWhatsAppRequest.RecipientItem item : req.getRecipients()) {
            Student student = null;
            if (item.getStudentId() != null) {
                student = studentRepository.findByIdAndSchoolId(item.getStudentId(), schoolId).orElse(null);
            }

            String message = item.getCustomMessage();
            if (message == null || message.isBlank()) {
                message = String.format(
                        "Dear %s, your child %s has been marked ABSENT on %s at %s. If this is unexpected, please contact the school office at %s.",
                        item.getParentName(),
                        item.getStudentName(),
                        todayStr,
                        school.getName(),
                        school.getPhone() != null ? school.getPhone() : "the main desk"
                );
            }

            String metaMsgId = "wamid." + UUID.randomUUID().toString().replace("-", "");

            WhatsAppLog logEntry = WhatsAppLog.builder()
                    .school(school)
                    .recipientName(item.getParentName())
                    .recipientPhone(item.getParentPhone())
                    .student(student)
                    .studentName(item.getStudentName())
                    .templateType(req.getTemplateType() != null ? req.getTemplateType() : "ABSENCE_ALERT")
                    .messageContent(message)
                    .status("DELIVERED")
                    .metaMessageId(metaMsgId)
                    .sentAt(Instant.now())
                    .build();

            WhatsAppLog saved = whatsAppLogRepository.save(logEntry);
            delivered.add(toDto(saved));
        }

        auditService.log("WHATSAPP_ABSENCE_BLAST", "whatsapp_logs", null,
                String.format("Dispatched %d absence alerts for date %s", delivered.size(), todayStr));

        return delivered;
    }

    private WhatsAppNotificationDto toDto(WhatsAppLog l) {
        return WhatsAppNotificationDto.builder()
                .id(l.getId())
                .recipientName(l.getRecipientName())
                .recipientPhone(l.getRecipientPhone())
                .studentId(l.getStudent() != null ? l.getStudent().getId() : null)
                .studentName(l.getStudentName())
                .templateType(l.getTemplateType())
                .messageContent(l.getMessageContent())
                .status(l.getStatus())
                .metaMessageId(l.getMetaMessageId())
                .sentAt(l.getSentAt())
                .build();
    }
}
