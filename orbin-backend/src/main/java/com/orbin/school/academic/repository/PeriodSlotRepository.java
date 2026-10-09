package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.PeriodSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeriodSlotRepository extends JpaRepository<PeriodSlot, Long> {
    List<PeriodSlot> findBySchoolIdOrderBySlotNumberAsc(Long schoolId);
    Optional<PeriodSlot> findBySchoolIdAndSlotNumber(Long schoolId, Integer slotNumber);
    Optional<PeriodSlot> findByIdAndSchoolId(Long id, Long schoolId);
}
