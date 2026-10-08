package com.orbin.school.fee.repository;

import com.orbin.school.fee.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findBySchoolIdOrderByCreatedAtDesc(Long schoolId, Pageable pageable);
    List<Payment> findBySchoolIdAndStudentIdOrderByPaymentDateDesc(Long schoolId, Long studentId);
    List<Payment> findBySchoolIdAndStudentFeeIdOrderByPaymentDateDesc(Long schoolId, Long studentFeeId);
    Optional<Payment> findByIdAndSchoolId(Long id, Long schoolId);
    Optional<Payment> findByReceiptNumber(String receiptNumber);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.school.id = :schoolId AND p.paymentDate = :date")
    BigDecimal sumPaymentsForDate(@Param("schoolId") Long schoolId, @Param("date") LocalDate date);
}
