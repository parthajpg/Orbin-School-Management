package com.orbin.school.whatsapp.repository;

import com.orbin.school.whatsapp.entity.WhatsAppLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WhatsAppLogRepository extends JpaRepository<WhatsAppLog, Long> {

    List<WhatsAppLog> findBySchoolIdOrderBySentAtDesc(Long schoolId);

    List<WhatsAppLog> findTop50BySchoolIdOrderBySentAtDesc(Long schoolId);
}
