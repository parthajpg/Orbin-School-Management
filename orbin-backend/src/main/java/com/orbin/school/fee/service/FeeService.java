package com.orbin.school.fee.service;

import com.orbin.school.academic.entity.AcademicYear;
import com.orbin.school.academic.entity.SchoolClass;
import com.orbin.school.academic.repository.AcademicYearRepository;
import com.orbin.school.academic.repository.SchoolClassRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.BusinessException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.fee.dto.*;
import com.orbin.school.fee.entity.*;
import com.orbin.school.fee.repository.*;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.student.entity.Student;
import com.orbin.school.student.repository.StudentRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeStructureRepository feeStructureRepository;
    private final FeeItemRepository feeItemRepository;
    private final StudentFeeRepository studentFeeRepository;
    private final PaymentRepository paymentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Fee Structures
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public FeeStructureDto createFeeStructure(CreateFeeStructureRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));

        SchoolClass sc = null;
        if (req.classId() != null) {
            sc = schoolClassRepository.findByIdAndSchoolId(req.classId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("SchoolClass", req.classId()));
        }

        BigDecimal total = req.items().stream()
                .map(CreateFeeStructureRequest.FeeItemEntry::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        FeeStructure structure = FeeStructure.builder()
                .school(school)
                .academicYear(ay)
                .schoolClass(sc)
                .name(req.name())
                .description(req.description())
                .totalAmount(total)
                .build();

        FeeStructure saved = feeStructureRepository.save(structure);

        List<FeeItem> items = new ArrayList<>();
        for (CreateFeeStructureRequest.FeeItemEntry itemEntry : req.items()) {
            FeeItem item = FeeItem.builder()
                    .feeStructure(saved)
                    .school(school)
                    .category(itemEntry.category())
                    .name(itemEntry.name())
                    .amount(itemEntry.amount())
                    .dueDate(itemEntry.dueDate())
                    .build();
            items.add(feeItemRepository.save(item));
        }

        saved.setItems(items);
        auditService.logAsync("CREATE_FEE_STRUCTURE", "FeeStructure", saved.getId().toString(), null, saved);
        return toFeeStructureDto(saved);
    }

    @Transactional(readOnly = true)
    public List<FeeStructureDto> getFeeStructures(Long academicYearId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<FeeStructure> list = academicYearId != null
                ? feeStructureRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId)
                : feeStructureRepository.findBySchoolId(schoolId);
        return list.stream().map(this::toFeeStructureDto).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Assign Fees
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public List<StudentFeeDto> assignFee(AssignFeeRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        FeeStructure structure = feeStructureRepository.findByIdAndSchoolId(req.feeStructureId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", req.feeStructureId()));

        List<Student> targets = new ArrayList<>();
        if (req.studentIds() != null && !req.studentIds().isEmpty()) {
            for (Long sId : req.studentIds()) {
                studentRepository.findByIdAndSchoolId(sId, schoolId).ifPresent(targets::add);
            }
        } else if (req.sectionId() != null) {
            targets.addAll(studentRepository.findBySchoolIdAndSectionId(schoolId, req.sectionId()));
        } else if (req.classId() != null) {
            targets.addAll(studentRepository.findBySchoolId(schoolId));
        }

        List<StudentFeeDto> assigned = new ArrayList<>();
        for (Student student : targets) {
            Optional<StudentFee> existing = studentFeeRepository
                    .findBySchoolIdAndStudentIdAndFeeStructureId(schoolId, student.getId(), structure.getId());

            if (existing.isEmpty()) {
                StudentFee sf = StudentFee.builder()
                        .school(school)
                        .student(student)
                        .feeStructure(structure)
                        .academicYear(structure.getAcademicYear())
                        .totalAmount(structure.getTotalAmount())
                        .paidAmount(BigDecimal.ZERO)
                        .dueDate(req.dueDate())
                        .status(StudentFee.FeeStatus.PENDING)
                        .build();

                StudentFee saved = studentFeeRepository.save(sf);
                assigned.add(toStudentFeeDto(saved));
            } else {
                assigned.add(toStudentFeeDto(existing.get()));
            }
        }

        auditService.logAsync("ASSIGN_FEES", "FeeStructure", req.feeStructureId().toString(), null,
                "Assigned to " + assigned.size() + " students");

        return assigned;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Payments & Receipts
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public PaymentDto recordPayment(RecordPaymentRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        StudentFee sf = studentFeeRepository.findByIdAndSchoolId(req.studentFeeId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("StudentFee", req.studentFeeId()));

        BigDecimal remaining = sf.calculateOutstanding();
        if (req.amount().compareTo(remaining) > 0) {
            throw new BusinessException("Payment amount (" + req.amount() + ") exceeds outstanding balance (" + remaining + ")");
        }

        Payment.PaymentMethod method;
        try {
            method = Payment.PaymentMethod.valueOf(req.paymentMethod().toUpperCase());
        } catch (Exception e) {
            method = Payment.PaymentMethod.CASH;
        }

        String receiptNumber = "RCP-" + LocalDate.now().getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        User currentUser = getCurrentUser();

        Payment payment = Payment.builder()
                .school(school)
                .studentFee(sf)
                .student(sf.getStudent())
                .amount(req.amount())
                .paymentDate(req.paymentDate() != null ? req.paymentDate() : LocalDate.now())
                .paymentMethod(method)
                .referenceNumber(req.referenceNumber())
                .receiptNumber(receiptNumber)
                .notes(req.notes())
                .recordedBy(currentUser)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // Update StudentFee
        BigDecimal newPaid = (sf.getPaidAmount() != null ? sf.getPaidAmount() : BigDecimal.ZERO).add(req.amount());
        sf.setPaidAmount(newPaid);
        if (newPaid.compareTo(sf.getTotalAmount()) >= 0) {
            sf.setStatus(StudentFee.FeeStatus.PAID);
        } else {
            sf.setStatus(StudentFee.FeeStatus.PARTIAL);
        }
        studentFeeRepository.save(sf);

        auditService.logAsync("RECORD_PAYMENT", "Payment", savedPayment.getId().toString(), null,
                "Receipt: " + receiptNumber + ", Amount: " + req.amount());

        return toPaymentDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public List<StudentFeeDto> getAllStudentFees() {
        Long schoolId = TenantContext.requireSchoolId();
        return studentFeeRepository.findBySchoolId(schoolId)
                .stream().map(this::toStudentFeeDto).toList();
    }

    @Transactional(readOnly = true)
    public List<StudentFeeDto> getStudentFees(Long studentId) {
        Long schoolId = TenantContext.requireSchoolId();
        return studentFeeRepository.findBySchoolIdAndStudentId(schoolId, studentId)
                .stream().map(this::toStudentFeeDto).toList();
    }

    @Transactional(readOnly = true)
    public Page<PaymentDto> getPayments(Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        return paymentRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId, pageable)
                .map(this::toPaymentDto);
    }

    @Transactional(readOnly = true)
    public FeeDashboardDto getFeeDashboard() {
        Long schoolId = TenantContext.requireSchoolId();
        BigDecimal totalExpected = studentFeeRepository.sumTotalFees(schoolId);
        BigDecimal totalCollected = studentFeeRepository.sumPaidFees(schoolId);
        BigDecimal totalOutstanding = totalExpected.subtract(totalCollected);

        long countAll = studentFeeRepository.count();

        return new FeeDashboardDto(
                totalExpected,
                totalCollected,
                totalOutstanding,
                countAll,
                0,
                0
        );
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private FeeStructureDto toFeeStructureDto(FeeStructure fs) {
        List<FeeItemDto> itemDtos = fs.getItems() != null
                ? fs.getItems().stream()
                    .map(i -> new FeeItemDto(i.getId(), i.getCategory(), i.getName(), i.getAmount(), i.getDueDate()))
                    .toList()
                : Collections.emptyList();

        return new FeeStructureDto(
                fs.getId(),
                fs.getAcademicYear().getId(),
                fs.getAcademicYear().getName(),
                fs.getSchoolClass() != null ? fs.getSchoolClass().getId() : null,
                fs.getSchoolClass() != null ? fs.getSchoolClass().getName() : null,
                fs.getName(),
                fs.getDescription(),
                fs.getTotalAmount(),
                itemDtos
        );
    }

    private StudentFeeDto toStudentFeeDto(StudentFee sf) {
        return new StudentFeeDto(
                sf.getId(),
                sf.getStudent().getId(),
                sf.getStudent().getFullName(),
                sf.getStudent().getAdmissionNumber(),
                sf.getFeeStructure().getId(),
                sf.getFeeStructure().getName(),
                sf.getAcademicYear().getId(),
                sf.getTotalAmount(),
                sf.getPaidAmount(),
                sf.calculateOutstanding(),
                sf.getDueDate(),
                sf.getStatus().name()
        );
    }

    private PaymentDto toPaymentDto(Payment p) {
        return new PaymentDto(
                p.getId(),
                p.getStudentFee().getId(),
                p.getStudent().getId(),
                p.getStudent().getFullName(),
                p.getStudent().getAdmissionNumber(),
                p.getAmount(),
                p.getPaymentDate(),
                p.getPaymentMethod().name(),
                p.getReferenceNumber(),
                p.getReceiptNumber(),
                p.getNotes(),
                p.getRecordedBy() != null ? p.getRecordedBy().getFullName() : null,
                p.getCreatedAt()
        );
    }
}
