package com.orbin.school.content.service;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.entity.Subject;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.academic.repository.SubjectRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.content.dto.*;
import com.orbin.school.content.entity.*;
import com.orbin.school.content.repository.*;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.syllabus.entity.SyllabusChapter;
import com.orbin.school.syllabus.repository.SyllabusChapterRepository;
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

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeworkService {

    private final HomeworkRepository homeworkRepository;
    private final MaterialRepository materialRepository;
    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final SyllabusChapterRepository chapterRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Homework
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public HomeworkDto createHomework(CreateHomeworkRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));
        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        User currentUser = getCurrentUser();

        Homework hw = Homework.builder()
                .school(school)
                .section(section)
                .subject(subject)
                .teacher(currentUser)
                .title(req.title())
                .description(req.description())
                .dueDate(req.dueDate())
                .attachmentUrl(req.attachmentUrl())
                .status(Homework.HomeworkStatus.PUBLISHED)
                .build();

        Homework saved = homeworkRepository.save(hw);
        auditService.logAsync("CREATE_HOMEWORK", "Homework", saved.getId().toString(), null, saved);
        return toHomeworkDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<HomeworkDto> getHomework(Long sectionId, Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        return homeworkRepository.findBySchoolIdAndSectionIdOrderByDueDateDesc(schoolId, sectionId, pageable)
                .map(this::toHomeworkDto);
    }

    @Transactional(readOnly = true)
    public List<HomeworkDto> getTodayHomework(Long sectionId) {
        Long schoolId = TenantContext.requireSchoolId();
        return homeworkRepository.findBySchoolIdAndSectionIdAndDueDate(schoolId, sectionId, LocalDate.now())
                .stream().map(this::toHomeworkDto).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Materials
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public MaterialDto createMaterial(CreateMaterialRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        SyllabusChapter chapter = null;
        if (req.chapterId() != null) {
            chapter = chapterRepository.findByIdAndSchoolId(req.chapterId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("SyllabusChapter", req.chapterId()));
        }

        Material.MaterialType type = Material.MaterialType.PDF;
        if (req.materialType() != null) {
            try {
                type = Material.MaterialType.valueOf(req.materialType().toUpperCase());
            } catch (Exception ignored) {}
        }

        User currentUser = getCurrentUser();

        Material material = Material.builder()
                .school(school)
                .subject(subject)
                .chapter(chapter)
                .teacher(currentUser)
                .title(req.title())
                .description(req.description())
                .materialType(type)
                .fileUrl(req.fileUrl())
                .externalUrl(req.externalUrl())
                .build();

        Material saved = materialRepository.save(material);
        auditService.logAsync("CREATE_MATERIAL", "Material", saved.getId().toString(), null, saved);
        return toMaterialDto(saved);
    }

    @Transactional(readOnly = true)
    public Page<MaterialDto> getMaterials(Long subjectId, Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        return materialRepository.findBySchoolIdAndSubjectIdOrderByCreatedAtDesc(schoolId, subjectId, pageable)
                .map(this::toMaterialDto);
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private HomeworkDto toHomeworkDto(Homework hw) {
        return new HomeworkDto(
                hw.getId(),
                hw.getSection().getId(),
                hw.getSection().getName(),
                hw.getSubject().getId(),
                hw.getSubject().getName(),
                hw.getTeacher() != null ? hw.getTeacher().getFullName() : null,
                hw.getTitle(),
                hw.getDescription(),
                hw.getDueDate(),
                hw.getAttachmentUrl(),
                hw.getStatus().name(),
                hw.getCreatedAt()
        );
    }

    private MaterialDto toMaterialDto(Material m) {
        return new MaterialDto(
                m.getId(),
                m.getSubject().getId(),
                m.getSubject().getName(),
                m.getChapter() != null ? m.getChapter().getId() : null,
                m.getChapter() != null ? m.getChapter().getTitle() : null,
                m.getTeacher() != null ? m.getTeacher().getFullName() : null,
                m.getTitle(),
                m.getDescription(),
                m.getMaterialType().name(),
                m.getFileUrl(),
                m.getExternalUrl(),
                m.getCreatedAt()
        );
    }
}
