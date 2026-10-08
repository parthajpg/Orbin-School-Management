package com.orbin.school.fee.entity;

import com.orbin.school.academic.entity.AcademicYear;
import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.school.entity.School;
import com.orbin.school.student.entity.Student;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "student_fees",
       uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "student_id", "fee_structure_id"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class StudentFee extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "outstanding", insertable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal outstanding;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private FeeStatus status = FeeStatus.PENDING;

    public enum FeeStatus { PENDING, PARTIAL, PAID, OVERDUE }

    public BigDecimal calculateOutstanding() {
        if (totalAmount == null) return BigDecimal.ZERO;
        BigDecimal paid = paidAmount != null ? paidAmount : BigDecimal.ZERO;
        return totalAmount.subtract(paid);
    }
}
