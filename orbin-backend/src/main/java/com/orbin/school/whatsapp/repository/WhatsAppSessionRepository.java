package com.orbin.school.whatsapp.repository;

import com.orbin.school.whatsapp.entity.WhatsAppSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface WhatsAppSessionRepository extends JpaRepository<WhatsAppSession, Long> {

    Optional<WhatsAppSession> findByPhoneNumber(String phoneNumber);

    @Modifying
    @Query("DELETE FROM WhatsAppSession s WHERE s.lastActivity < :cutoff")
    void deleteStaleSessionsOlderThan(@Param("cutoff") Instant cutoff);
}
