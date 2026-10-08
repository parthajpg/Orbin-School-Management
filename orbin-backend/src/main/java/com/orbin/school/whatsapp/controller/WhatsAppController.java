package com.orbin.school.whatsapp.controller;

import com.orbin.school.common.ApiResponse;
import com.orbin.school.whatsapp.dto.SendWhatsAppRequest;
import com.orbin.school.whatsapp.dto.WhatsAppNotificationDto;
import com.orbin.school.whatsapp.service.WhatsAppService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/whatsapp")
@RequiredArgsConstructor
public class WhatsAppController {

    private final WhatsAppService whatsAppService;

    @GetMapping("/logs")
    public ResponseEntity<ApiResponse<List<WhatsAppNotificationDto>>> getRecentLogs() {
        return ResponseEntity.ok(ApiResponse.ok(whatsAppService.getRecentLogs()));
    }

    @PostMapping("/send-absence-blast")
    public ResponseEntity<ApiResponse<List<WhatsAppNotificationDto>>> sendAbsenceBlast(
            @Valid @RequestBody SendWhatsAppRequest request) {
        List<WhatsAppNotificationDto> sent = whatsAppService.sendAbsenceBlast(request);
        return ResponseEntity.ok(ApiResponse.ok(sent));
    }
}
