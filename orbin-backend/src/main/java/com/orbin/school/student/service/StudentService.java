package com.orbin.school.student.service;

import com.orbin.school.academic.entity.AcademicYear;
import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.repository.AcademicYearRepository;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.DuplicateResourceException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.student.dto.CreateStudentRequest;
import com.orbin.school.student.dto.StudentResponse;
import com.orbin.school.student.entity.Student;
import com.orbin.school.student.repository.StudentRepository;
import com.orbin.school.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Student management service.
 *
 * <p><strong>Tenant isolation:</strong> All methods resolve schoolId from
 * {@link TenantContext}. No method accepts schoolId as a parameter from
 * the controller layer.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository    studentRepository;
    private final SectionRepository    sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolRepository     schoolRepository;
    private final AuditService         auditService;

    // ── Create ────────────────────────────────────────────────────────

    @Transactional
    @PreAuthorize("hasAuthority('students.create')")
    public StudentResponse createStudent(CreateStudentRequest request, Long academicYearId) {
        Long schoolId = TenantContext.requireSchoolId();

        // Validate section belongs to this school
        Section section = sectionRepository.findByIdAndSchoolId(request.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Section not found or does not belong to this school"));

        // Validate academic year belongs to this school
        AcademicYear academicYear;
        if (academicYearId != null) {
            academicYear = academicYearRepository
                    .findByIdAndSchoolId(academicYearId, schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Academic year not found"));
        } else {
            academicYear = academicYearRepository.findBySchoolIdAndCurrentTrue(schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("No active academic year found for school"));
        }

        // Unique admission number within school
        if (studentRepository.existsBySchoolIdAndAdmissionNumber(schoolId, request.admissionNumber())) {
            throw new DuplicateResourceException(
                    "Admission number already exists: " + request.admissionNumber());
        }

        School school = schoolRepository.getReferenceById(schoolId);

        Student student = Student.builder()
                .school(school)
                .academicYear(academicYear)
                .admissionNumber(request.admissionNumber())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .dateOfBirth(request.dateOfBirth())
                .gender(request.gender())
                .section(section)
                .admissionDate(request.admissionDate())
                .address(request.address())
                .build();

        student = studentRepository.save(student);
        auditService.log("CREATE", "Student", student.getId().toString());

        log.info("Student created: admNo={} school={}", student.getAdmissionNumber(), schoolId);
        return StudentResponse.from(student);
    }

    @Transactional
    @PreAuthorize("hasAuthority('students.create')")
    public List<StudentResponse> bulkCreateStudents(List<CreateStudentRequest> requests, Long academicYearId) {
        return requests.stream()
                .map(req -> createStudent(req, academicYearId))
                .toList();
    }

    // ── Read ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('students.read')")
    public StudentResponse getStudent(Long studentId) {
        Long schoolId = TenantContext.requireSchoolId();
        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        return StudentResponse.from(student);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('students.read')")
    public Page<StudentResponse> listStudents(Long academicYearId, String query, Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        if (query != null && !query.isBlank()) {
            return studentRepository.searchBySchool(schoolId, query, pageable)
                    .map(StudentResponse::from);
        }
        if (academicYearId != null) {
            return studentRepository
                    .findBySchoolIdAndAcademicYearId(schoolId, academicYearId, pageable)
                    .map(StudentResponse::from);
        }
        return academicYearRepository.findBySchoolIdAndCurrentTrue(schoolId)
                .map(ay -> studentRepository.findBySchoolIdAndAcademicYearId(schoolId, ay.getId(), pageable))
                .orElseGet(() -> studentRepository.findBySchoolId(schoolId, pageable))
                .map(StudentResponse::from);
    }

    // ── Update ────────────────────────────────────────────────────────

    @Transactional
    @PreAuthorize("hasAuthority('students.update')")
    public StudentResponse updateStudent(Long studentId, CreateStudentRequest request) {
        Long schoolId = TenantContext.requireSchoolId();
        Student student = studentRepository.findByIdAndSchoolId(studentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        Section section = sectionRepository.findByIdAndSchoolId(request.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section not found"));

        student.setFirstName(request.firstName());
        student.setLastName(request.lastName());
        student.setDateOfBirth(request.dateOfBirth());
        student.setGender(request.gender());
        student.setSection(section);
        student.setAddress(request.address());

        student = studentRepository.save(student);
        auditService.log("UPDATE", "Student", student.getId().toString());
        return StudentResponse.from(student);
    }
}
