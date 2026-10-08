package com.orbin.school.academic.service;

import com.orbin.school.academic.dto.*;
import com.orbin.school.academic.entity.*;
import com.orbin.school.academic.repository.*;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import com.orbin.school.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicService {

    private final AcademicYearRepository academicYearRepository;
    private final SchoolClassRepository schoolClassRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherAssignmentRepository teacherAssignmentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Academic Years
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AcademicYearDto> getAcademicYears() {
        Long schoolId = TenantContext.requireSchoolId();
        return academicYearRepository.findBySchoolId(schoolId).stream()
                .map(this::toAcademicYearDto)
                .toList();
    }

    @Transactional
    public AcademicYearDto createAcademicYear(CreateAcademicYearRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        if (req.current()) {
            academicYearRepository.findBySchoolIdAndCurrentTrue(schoolId)
                    .ifPresent(ay -> ay.setCurrent(false));
        }

        AcademicYear ay = AcademicYear.builder()
                .school(school)
                .name(req.name())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .current(req.current())
                .status(AcademicYear.Status.ACTIVE)
                .build();

        AcademicYear saved = academicYearRepository.save(ay);
        auditService.logAsync("CREATE_ACADEMIC_YEAR", "AcademicYear", saved.getId().toString(), null, saved);
        return toAcademicYearDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Classes
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SchoolClassDto> getClasses(Long academicYearId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<SchoolClass> list = academicYearId != null
                ? schoolClassRepository.findBySchoolIdAndAcademicYearIdOrderByDisplayOrderAsc(schoolId, academicYearId)
                : schoolClassRepository.findBySchoolIdOrderByDisplayOrderAsc(schoolId);
        return list.stream().map(this::toClassDto).toList();
    }

    @Transactional
    public SchoolClassDto createClass(CreateClassRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));

        SchoolClass sc = SchoolClass.builder()
                .school(school)
                .academicYear(ay)
                .name(req.name())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();

        SchoolClass saved = schoolClassRepository.save(sc);
        auditService.logAsync("CREATE_CLASS", "SchoolClass", saved.getId().toString(), null, saved);
        return toClassDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sections
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SectionDto> getSections(Long classId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<Section> list = classId != null
                ? sectionRepository.findBySchoolIdAndSchoolClassId(schoolId, classId)
                : sectionRepository.findBySchoolId(schoolId);
        return list.stream().map(this::toSectionDto).toList();
    }

    @Transactional
    public SectionDto createSection(CreateSectionRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        SchoolClass sc = schoolClassRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolClass", req.classId()));

        User teacher = null;
        if (req.classTeacherId() != null) {
            teacher = userRepository.findByIdAndSchoolId(req.classTeacherId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.classTeacherId()));
        }

        Section section = Section.builder()
                .school(school)
                .schoolClass(sc)
                .name(req.name())
                .capacity(req.capacity())
                .classTeacher(teacher)
                .build();

        Section saved = sectionRepository.save(section);
        auditService.logAsync("CREATE_SECTION", "Section", saved.getId().toString(), null, saved);
        return toSectionDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Subjects
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SubjectDto> getSubjects(Long classId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<Subject> list = classId != null
                ? subjectRepository.findBySchoolIdAndSchoolClassId(schoolId, classId)
                : subjectRepository.findBySchoolId(schoolId);
        return list.stream().map(this::toSubjectDto).toList();
    }

    @Transactional
    public SubjectDto createSubject(CreateSubjectRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        SchoolClass sc = schoolClassRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolClass", req.classId()));

        Subject.SubjectType type = Subject.SubjectType.ACADEMIC;
        if (req.subjectType() != null) {
            try {
                type = Subject.SubjectType.valueOf(req.subjectType().toUpperCase());
            } catch (Exception ignored) {}
        }

        Subject subject = Subject.builder()
                .school(school)
                .schoolClass(sc)
                .name(req.name())
                .code(req.code())
                .subjectType(type)
                .build();

        Subject saved = subjectRepository.save(subject);
        auditService.logAsync("CREATE_SUBJECT", "Subject", saved.getId().toString(), null, saved);
        return toSubjectDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Teacher Assignments
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TeacherAssignmentDto> getTeacherAssignments(Long academicYearId, Long teacherId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<TeacherAssignment> list;
        if (teacherId != null) {
            list = teacherAssignmentRepository.findBySchoolIdAndTeacherId(schoolId, teacherId);
        } else if (academicYearId != null) {
            list = teacherAssignmentRepository.findBySchoolIdAndAcademicYearId(schoolId, academicYearId);
        } else {
            list = teacherAssignmentRepository.findBySchoolId(schoolId);
        }
        return list.stream().map(this::toAssignmentDto).toList();
    }

    @Transactional
    public TeacherAssignmentDto assignTeacher(AssignTeacherRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));
        User teacher = userRepository.findByIdAndSchoolId(req.teacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.teacherId()));
        SchoolClass sc = schoolClassRepository.findByIdAndSchoolId(req.classId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("SchoolClass", req.classId()));
        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));
        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        TeacherAssignment ta = TeacherAssignment.builder()
                .school(school)
                .academicYear(ay)
                .teacher(teacher)
                .schoolClass(sc)
                .section(section)
                .subject(subject)
                .build();

        TeacherAssignment saved = teacherAssignmentRepository.save(ta);
        auditService.logAsync("ASSIGN_TEACHER", "TeacherAssignment", saved.getId().toString(), null, saved);
        return toAssignmentDto(saved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private AcademicYearDto toAcademicYearDto(AcademicYear ay) {
        return new AcademicYearDto(ay.getId(), ay.getName(), ay.getStartDate(), ay.getEndDate(), ay.isCurrent(), ay.getStatus().name());
    }

    private SchoolClassDto toClassDto(SchoolClass sc) {
        return new SchoolClassDto(sc.getId(), sc.getAcademicYear() != null ? sc.getAcademicYear().getId() : null, sc.getName(), sc.getDisplayOrder());
    }

    private SectionDto toSectionDto(Section s) {
        String teacherName = s.getClassTeacher() != null ? s.getClassTeacher().getFullName() : null;
        return new SectionDto(s.getId(), s.getSchoolClass().getId(), s.getSchoolClass().getName(), s.getName(), s.getCapacity(),
                s.getClassTeacher() != null ? s.getClassTeacher().getId() : null, teacherName);
    }

    private SubjectDto toSubjectDto(Subject sub) {
        return new SubjectDto(sub.getId(), sub.getSchoolClass().getId(), sub.getName(), sub.getCode(), sub.getSubjectType().name());
    }

    private TeacherAssignmentDto toAssignmentDto(TeacherAssignment ta) {
        return new TeacherAssignmentDto(
                ta.getId(),
                ta.getAcademicYear().getId(),
                ta.getTeacher().getId(),
                ta.getTeacher().getFullName(),
                ta.getSchoolClass().getId(),
                ta.getSchoolClass().getName(),
                ta.getSection().getId(),
                ta.getSection().getName(),
                ta.getSubject().getId(),
                ta.getSubject().getName()
        );
    }
}
