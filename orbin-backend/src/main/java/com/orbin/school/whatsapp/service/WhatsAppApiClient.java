package com.orbin.school.whatsapp.service;

import com.orbin.school.common.JwtProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Sends WhatsApp messages via Meta Cloud API.
 *
 * <p>Uses text messages for V1. Interactive list messages can be added later.
 */
@Slf4j
@Service
public class WhatsAppApiClient {

    @Value("${orbin.whatsapp.token}")
    private String token;

    @Value("${orbin.whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${orbin.whatsapp.api-version}")
    private String apiVersion;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends a plain text message to a WhatsApp number.
     *
     * @param to   recipient number in +91XXXXXXXXXX format
     * @param body message body text
     */
    public void sendTextMessage(String to, String body) {
        if (token == null || token.isBlank() || phoneNumberId == null || phoneNumberId.isBlank()) {
            log.warn("WhatsApp credentials not configured — skipping message to {}", to);
            return;
        }

        String url = "https://graph.facebook.com/" + apiVersion
                + "/" + phoneNumberId + "/messages";

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", sanitizeNumber(to),
                "type", "text",
                "text", Map.of("body", body)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("WhatsApp send failed: status={} body={}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("WhatsApp send error to {}: {}", to, e.getMessage());
        }
    }

    /** Strips the + for Meta API which expects the number without + prefix */
    private String sanitizeNumber(String phone) {
        return phone.startsWith("+") ? phone.substring(1) : phone;
    }
}
