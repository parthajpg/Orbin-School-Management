package com.orbin.school.exam.service;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.entity.Subject;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.academic.repository.SubjectRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.exam.dto.*;
import com.orbin.school.exam.entity.Result;
import com.orbin.school.exam.entity.Test;
import com.orbin.school.exam.repository.ResultRepository;
import com.orbin.school.exam.repository.TestRepository;
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
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamService {

    private final TestRepository testRepository;
    private final ResultRepository resultRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    @Transactional
    public TestDto createTest(CreateTestRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));
        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        User currentUser = getCurrentUser();

        Test test = Test.builder()
                .school(school)
                .section(section)
                .subject(subject)
                .teacher(currentUser)
                .title(req.title())
                .testDate(req.testDate())
                .durationMin(req.durationMin())
                .maxMarks(req.maxMarks())
                .instructions(req.instructions())
                .status(Test.TestStatus.SCHEDULED)
                .build();

        Test saved = testRepository.save(test);
        auditService.logAsync("CREATE_TEST", "Test", saved.getId().toString(), null, saved);
        return toTestDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<TestDto> getTests(Long sectionId, Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        return testRepository.findBySchoolIdAndSectionIdOrderByTestDateDesc(schoolId, sectionId, pageable)
                .map(this::toTestDto);
    }

    @Transactional
    public List<ResultDto> enterMarks(EnterMarksRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        Test test = testRepository.findByIdAndSchoolId(req.testId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Test", req.testId()));

        User currentUser = getCurrentUser();
        List<ResultDto> savedResults = new ArrayList<>();

        for (EnterMarksRequest.StudentMarkEntry markEntry : req.marks()) {
            Student student = studentRepository.findByIdAndSchoolId(markEntry.studentId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", markEntry.studentId()));

            Result result = resultRepository
                    .findBySchoolIdAndTestIdAndStudentId(schoolId, test.getId(), student.getId())
                    .orElse(Result.builder()
                            .school(school)
                            .test(test)
                            .student(student)
                            .build());

            BigDecimal pct = BigDecimal.ZERO;
            if (test.getMaxMarks() != null && test.getMaxMarks().compareTo(BigDecimal.ZERO) > 0) {
                pct = markEntry.marksObtained()
                        .multiply(BigDecimal.valueOf(100))
                        .divide(test.getMaxMarks(), 2, RoundingMode.HALF_UP);
            }

            result.setMarksObtained(markEntry.marksObtained());
            result.setMaxMarks(test.getMaxMarks());
            result.setPercentage(pct);
            result.setTeacherNote(markEntry.teacherNote());
            result.setEnteredBy(currentUser);

            Result saved = resultRepository.save(result);
            savedResults.add(toResultDto(saved));
        }

        test.setStatus(Test.TestStatus.COMPLETED);
        testRepository.save(test);

        auditService.logAsync("ENTER_MARKS", "Test", test.getId().toString(), null,
                "Entered marks for " + req.marks().size() + " students");

        return savedResults;
    }

    @Transactional(readOnly = true)
    public List<ResultDto> getTestResults(Long testId) {
        Long schoolId = TenantContext.requireSchoolId();
        return resultRepository.findBySchoolIdAndTestId(schoolId, testId)
                .stream().map(this::toResultDto).toList();
    }

    @Transactional(readOnly = true)
    public List<ResultDto> getStudentReportCard(Long studentId) {
        Long schoolId = TenantContext.requireSchoolId();
        return resultRepository.findBySchoolIdAndStudentId(schoolId, studentId)
                .stream().map(this::toResultDto).toList();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private TestDto toTestDto(Test t) {
        return new TestDto(
                t.getId(),
                t.getSection().getId(),
                t.getSection().getName(),
                t.getSubject().getId(),
                t.getSubject().getName(),
                t.getTeacher() != null ? t.getTeacher().getFullName() : null,
                t.getTitle(),
                t.getTestDate(),
                t.getDurationMin(),
                t.getMaxMarks(),
                t.getInstructions(),
                t.getStatus().name()
        );
    }

    private ResultDto toResultDto(Result r) {
        return new ResultDto(
                r.getId(),
                r.getTest().getId(),
                r.getTest().getTitle(),
                r.getStudent().getId(),
                r.getStudent().getFullName(),
                r.getStudent().getAdmissionNumber(),
                r.getMarksObtained(),
                r.getMaxMarks(),
                r.getPercentage(),
                r.getTeacherNote()
        );
    }
}
