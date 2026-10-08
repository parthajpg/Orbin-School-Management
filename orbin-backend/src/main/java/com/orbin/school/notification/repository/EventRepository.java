package com.orbin.school.notification.repository;

import com.orbin.school.notification.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Page<Event> findBySchoolIdOrderByEventDateDesc(Long schoolId, Pageable pageable);
    List<Event> findBySchoolIdAndIsPublicTrueOrderByEventDateAsc(Long schoolId);
    Optional<Event> findByIdAndSchoolId(Long id, Long schoolId);
}
