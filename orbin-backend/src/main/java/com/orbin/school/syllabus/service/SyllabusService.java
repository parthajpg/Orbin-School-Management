package com.orbin.school.syllabus.service;

import com.orbin.school.academic.entity.AcademicYear;
import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.entity.Subject;
import com.orbin.school.academic.repository.AcademicYearRepository;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.academic.repository.SubjectRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.syllabus.dto.*;
import com.orbin.school.syllabus.entity.*;
import com.orbin.school.syllabus.repository.*;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyllabusService {

    private final TermRepository termRepository;
    private final SyllabusChapterRepository chapterRepository;
    private final SyllabusTopicRepository topicRepository;
    private final SyllabusProgressRepository progressRepository;
    private final SubjectRepository subjectRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SectionRepository sectionRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Terms
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public TermDto createTerm(CreateTermRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        AcademicYear ay = academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()));

        Term term = Term.builder()
                .school(school)
                .academicYear(ay)
                .name(req.name())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();

        Term saved = termRepository.save(term);
        auditService.logAsync("CREATE_TERM", "Term", saved.getId().toString(), null, saved);
        return toTermDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TermDto> getTerms(Long academicYearId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<Term> list = academicYearId != null
                ? termRepository.findBySchoolIdAndAcademicYearIdOrderByDisplayOrderAsc(schoolId, academicYearId)
                : termRepository.findBySchoolIdOrderByDisplayOrderAsc(schoolId);
        return list.stream().map(this::toTermDto).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Chapters & Topics
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public ChapterDto createChapter(CreateChapterRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        Subject subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", req.subjectId()));

        Term term = null;
        if (req.termId() != null) {
            term = termRepository.findByIdAndSchoolId(req.termId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Term", req.termId()));
        }

        SyllabusChapter chapter = SyllabusChapter.builder()
                .school(school)
                .subject(subject)
                .term(term)
                .title(req.title())
                .description(req.description())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();

        SyllabusChapter saved = chapterRepository.save(chapter);

        List<SyllabusTopic> topics = new ArrayList<>();
        if (req.topicTitles() != null) {
            int order = 1;
            for (String title : req.topicTitles()) {
                if (title != null && !title.isBlank()) {
                    SyllabusTopic topic = SyllabusTopic.builder()
                            .school(school)
                            .chapter(saved)
                            .title(title.trim())
                            .displayOrder(order++)
                            .build();
                    topics.add(topicRepository.save(topic));
                }
            }
        }
        saved.setTopics(topics);

        auditService.logAsync("CREATE_CHAPTER", "SyllabusChapter", saved.getId().toString(), null, saved);
        return toChapterDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ChapterDto> getChapters(Long subjectId, Long termId) {
        Long schoolId = TenantContext.requireSchoolId();
        List<SyllabusChapter> list = termId != null
                ? chapterRepository.findBySchoolIdAndSubjectIdAndTermIdOrderByDisplayOrderAsc(schoolId, subjectId, termId)
                : chapterRepository.findBySchoolIdAndSubjectIdOrderByDisplayOrderAsc(schoolId, subjectId);
        return list.stream().map(this::toChapterDto).toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Progress
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public SyllabusProgressDto updateProgress(UpdateProgressRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        SyllabusChapter chapter = chapterRepository.findByIdAndSchoolId(req.chapterId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("SyllabusChapter", req.chapterId()));
        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));

        SyllabusProgress.ProgressStatus status;
        try {
            status = SyllabusProgress.ProgressStatus.valueOf(req.status().toUpperCase());
        } catch (Exception e) {
            status = SyllabusProgress.ProgressStatus.IN_PROGRESS;
        }

        SyllabusProgress sp = progressRepository
                .findBySchoolIdAndChapterIdAndSectionId(schoolId, req.chapterId(), req.sectionId())
                .orElse(SyllabusProgress.builder()
                        .school(school)
                        .chapter(chapter)
                        .section(section)
                        .build());

        sp.setStatus(status);
        sp.setPlannedStartDate(req.plannedStartDate());
        sp.setPlannedCompletionDate(req.plannedCompletionDate());
        sp.setActualCompletionDate(req.actualCompletionDate());
        sp.setEstimatedPeriods(req.estimatedPeriods());
        sp.setActualPeriods(req.actualPeriods());
        sp.setNotes(req.notes());
        sp.setRevisionDate(req.revisionDate());

        User currentUser = getCurrentUser();
        if (currentUser != null) {
            sp.setTeacher(currentUser);
        }

        SyllabusProgress saved = progressRepository.save(sp);
        auditService.logAsync("UPDATE_SYLLABUS_PROGRESS", "SyllabusProgress", saved.getId().toString(), null,
                "Status: " + status + " for chapter: " + chapter.getTitle());

        return toProgressDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SyllabusProgressDto> getSectionProgress(Long sectionId) {
        Long schoolId = TenantContext.requireSchoolId();
        return progressRepository.findBySchoolIdAndSectionId(schoolId, sectionId)
                .stream().map(this::toProgressDto).toList();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private TermDto toTermDto(Term t) {
        return new TermDto(t.getId(), t.getAcademicYear().getId(), t.getName(), t.getStartDate(), t.getEndDate(), t.getDisplayOrder());
    }

    private ChapterDto toChapterDto(SyllabusChapter c) {
        List<TopicDto> topics = c.getTopics() != null
                ? c.getTopics().stream().map(tp -> new TopicDto(tp.getId(), c.getId(), tp.getTitle(), tp.getDisplayOrder())).toList()
                : Collections.emptyList();

        return new ChapterDto(
                c.getId(),
                c.getSubject().getId(),
                c.getSubject().getName(),
                c.getTerm() != null ? c.getTerm().getId() : null,
                c.getTerm() != null ? c.getTerm().getName() : null,
                c.getTitle(),
                c.getDescription(),
                c.getDisplayOrder(),
                topics
        );
    }

    private SyllabusProgressDto toProgressDto(SyllabusProgress sp) {
        return new SyllabusProgressDto(
                sp.getId(),
                sp.getChapter().getId(),
                sp.getChapter().getTitle(),
                sp.getSection().getId(),
                sp.getSection().getName(),
                sp.getTeacher() != null ? sp.getTeacher().getFullName() : null,
                sp.getStatus().name(),
                sp.getPlannedStartDate(),
                sp.getPlannedCompletionDate(),
                sp.getActualCompletionDate(),
                sp.getEstimatedPeriods(),
                sp.getActualPeriods(),
                sp.getNotes(),
                sp.getRevisionDate()
        );
    }
}
