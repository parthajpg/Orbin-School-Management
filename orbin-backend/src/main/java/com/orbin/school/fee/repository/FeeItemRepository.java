package com.orbin.school.fee.repository;

import com.orbin.school.fee.entity.FeeItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeItemRepository extends JpaRepository<FeeItem, Long> {
    List<FeeItem> findByFeeStructureId(Long feeStructureId);
    List<FeeItem> findBySchoolId(Long schoolId);
}
