package com.orbin.school.parent.service;

import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.DuplicateResourceException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.parent.dto.CreateParentRequest;
import com.orbin.school.parent.dto.LinkStudentRequest;
import com.orbin.school.parent.dto.ParentResponse;
import com.orbin.school.parent.entity.Parent;
import com.orbin.school.parent.entity.ParentStudent;
import com.orbin.school.parent.repository.ParentRepository;
import com.orbin.school.parent.repository.ParentStudentRepository;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.student.entity.Student;
import com.orbin.school.student.repository.StudentRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.whatsapp.service.PhoneNormalizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public Page<ParentResponse> searchParents(String query, Pageable pageable) {
        Long schoolId = TenantContext.requireSchoolId();
        Page<Parent> page = (query != null && !query.isBlank())
                ? parentRepository.searchParents(schoolId, query.trim(), pageable)
                : parentRepository.findAll(pageable);
        return page.map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ParentResponse getParentById(Long id) {
        Long schoolId = TenantContext.requireSchoolId();
        Parent parent = parentRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", id));
        return toResponse(parent);
    }

    @Transactional
    public ParentResponse createParent(CreateParentRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        String normalized = PhoneNormalizer.normalize(req.phone());

        parentRepository.findBySchoolIdAndNormalizedPhone(schoolId, normalized)
                .ifPresent(p -> {
                    throw new DuplicateResourceException("Parent with phone " + req.phone() + " already exists in this school");
                });

        School school = schoolRepository.getReferenceById(schoolId);
        Parent parent = Parent.builder()
                .school(school)
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .normalizedPhone(normalized)
                .email(req.email())
                .occupation(req.occupation())
                .address(req.address())
                .whatsappVerified(false)
                .build();

        Parent saved = parentRepository.save(parent);
        auditService.logAsync("CREATE_PARENT", "Parent", saved.getId().toString(), null, saved);
        return toResponse(saved);
    }

    @Transactional
    public ParentResponse linkStudent(Long parentId, LinkStudentRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        Parent parent = parentRepository.findByIdAndSchoolId(parentId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", parentId));

        Student student = studentRepository.findByIdAndSchoolId(req.studentId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", req.studentId()));

        ParentStudent ps = parentStudentRepository.findByParentIdAndStudentId(parentId, req.studentId())
                .orElse(ParentStudent.builder()
                        .parent(parent)
                        .student(student)
                        .build());

        ps.setRelationship(req.relationship());
        ps.setPrimary(req.primary());
        ps.setCanReceiveNotifications(req.canReceiveNotifications());

        parentStudentRepository.save(ps);
        auditService.logAsync("LINK_PARENT_STUDENT", "ParentStudent", parentId + "-" + req.studentId(), null, ps);

        return toResponse(parent);
    }

    private ParentResponse toResponse(Parent p) {
        List<ParentStudent> links = parentStudentRepository.findByParentId(p.getId());
        List<ParentResponse.LinkedStudentDto> studentDtos = links.stream()
                .map(l -> new ParentResponse.LinkedStudentDto(
                        l.getStudent().getId(),
                        l.getStudent().getAdmissionNumber(),
                        l.getStudent().getFullName(),
                        l.getRelationship(),
                        l.isPrimary()
                ))
                .toList();

        return new ParentResponse(
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                p.getFullName(),
                p.getPhone(),
                p.getNormalizedPhone(),
                p.getEmail(),
                p.getOccupation(),
                p.getAddress(),
                p.isWhatsappVerified(),
                studentDtos,
                p.getCreatedAt()
        );
    }
}
