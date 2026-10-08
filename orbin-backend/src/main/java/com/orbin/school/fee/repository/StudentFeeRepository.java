package com.orbin.school.fee.repository;

import com.orbin.school.fee.entity.StudentFee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface StudentFeeRepository extends JpaRepository<StudentFee, Long> {
    List<StudentFee> findBySchoolIdAndStudentId(Long schoolId, Long studentId);
    Optional<StudentFee> findByIdAndSchoolId(Long id, Long schoolId);
    Optional<StudentFee> findBySchoolIdAndStudentIdAndFeeStructureId(Long schoolId, Long studentId, Long feeStructureId);
    Page<StudentFee> findBySchoolId(Long schoolId, Pageable pageable);
    List<StudentFee> findBySchoolId(Long schoolId);
    Page<StudentFee> findBySchoolIdAndStatus(Long schoolId, StudentFee.FeeStatus status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(sf.totalAmount), 0) FROM StudentFee sf WHERE sf.school.id = :schoolId")
    BigDecimal sumTotalFees(@Param("schoolId") Long schoolId);

    @Query("SELECT COALESCE(SUM(sf.paidAmount), 0) FROM StudentFee sf WHERE sf.school.id = :schoolId")
    BigDecimal sumPaidFees(@Param("schoolId") Long schoolId);
}
