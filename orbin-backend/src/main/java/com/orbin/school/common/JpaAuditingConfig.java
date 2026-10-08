package com.orbin.school.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import com.orbin.school.tenant.TenantContext;

import java.util.Optional;

/**
 * Enables JPA auditing for createdAt / updatedAt fields.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(TenantContext.getUserEmail())
                .or(() -> Optional.of("system"));
    }
}
