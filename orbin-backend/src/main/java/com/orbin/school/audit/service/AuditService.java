package com.orbin.school.audit.service;

import com.orbin.school.audit.entity.AuditLog;
import com.orbin.school.audit.repository.AuditLogRepository;
import com.orbin.school.school.entity.School;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Logs sensitive operations asynchronously so they never block the main flow.
 * Runs in a separate transaction to survive rollbacks in the calling transaction.
 *
 * <p>Safely captures tenant and user context on the calling thread before
 * async dispatch to prevent context loss across thread boundaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void logAsync(String action, String entityType, String entityId,
                         Map<String, Object> previousValue, Map<String, Object> newValue) {
        Long schoolId = TenantContext.getSchoolId();
        Long userId   = TenantContext.getUserId();
        saveAuditAsync(schoolId, userId, action, entityType, entityId, previousValue, newValue, null);
    }

    public void logAsync(String action, String entityType, String entityId,
                         Map<String, Object> previousValue, Object newValueObj) {
        Long schoolId = TenantContext.getSchoolId();
        Long userId   = TenantContext.getUserId();
        Map<String, Object> newMap = newValueObj != null
                ? Map.of("summary", newValueObj.toString())
                : null;
        saveAuditAsync(schoolId, userId, action, entityType, entityId, previousValue, newMap, null);
    }

    public void log(String action, String entityType, String entityId) {
        logAsync(action, entityType, entityId, null, (Map<String, Object>) null);
    }

    public void log(String action, String entityType, Object entityId) {
        String idStr = entityId != null ? entityId.toString() : null;
        log(action, entityType, idStr);
    }

    public void log(String action, String entityType, Object entityId, String details) {
        String idStr = entityId != null ? entityId.toString() : null;
        logAsync(action, entityType, idStr, null, details);
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditAsync(Long schoolId, Long userId, String action, String entityType, String entityId,
                               Map<String, Object> previousValue, Map<String, Object> newValue,
                               String ipAddress) {
        try {
            School school = null;
            if (schoolId != null) {
                school = new School();
                school.setId(schoolId);
            }
            User user = null;
            if (userId != null) {
                user = new User();
                user.setId(userId);
            }

            AuditLog logEntry = AuditLog.builder()
                    .school(school)
                    .user(user)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .previousValue(previousValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            // Never let audit failure break business logic
            log.error("Failed to write audit log: action={} entity={}/{} error={}",
                    action, entityType, entityId, e.getMessage());
        }
    }
}
