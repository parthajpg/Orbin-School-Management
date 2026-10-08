package com.orbin.school.staff.service;

import com.orbin.school.academic.entity.SchoolClass;
import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.repository.SchoolClassRepository;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.DuplicateResourceException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.staff.dto.CreateStaffRequest;
import com.orbin.school.staff.dto.StaffDto;
import com.orbin.school.staff.entity.Staff;
import com.orbin.school.staff.repository.StaffRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import com.orbin.school.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository       staffRepository;
    private final SchoolRepository      schoolRepository;
    private final UserRepository        userRepository;
    private final SchoolClassRepository classRepository;
    private final SectionRepository    sectionRepository;
    private final PasswordEncoder       passwordEncoder;
    private final AuditService          auditService;

    // ── Get All Staff for Tenant School ──────────────────────────────
    @Transactional(readOnly = true)
    public List<StaffDto> getAllStaff() {
        Long schoolId = TenantContext.requireSchoolId();
        return staffRepository.findBySchoolIdOrderByCreatedAtDesc(schoolId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── Get Staff by ID ──────────────────────────────────────────────
    @Transactional(readOnly = true)
    public StaffDto getStaffById(Long id) {
        Long schoolId = TenantContext.requireSchoolId();
        Staff staff = staffRepository.findBySchoolIdAndId(schoolId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with ID: " + id));
        return toDto(staff);
    }

    // ── Create Staff & Provision User Account ────────────────────────
    @Transactional
    public StaffDto createStaff(CreateStaffRequest request) {
        Long schoolId = TenantContext.requireSchoolId();

        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        String empId = (request.getEmployeeId() != null && !request.getEmployeeId().isBlank())
                ? request.getEmployeeId().trim()
                : "EMP-" + (System.currentTimeMillis() % 100000);

        if (staffRepository.existsBySchoolIdAndEmployeeId(schoolId, empId)) {
            throw new DuplicateResourceException("Staff member with Employee ID " + empId + " already exists in this school.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (staffRepository.existsBySchoolIdAndEmail(schoolId, email)) {
            throw new DuplicateResourceException("Staff member with email " + email + " already exists in this school.");
        }

        // Provision User Account if email not already taken
        User user = null;
        if (!userRepository.existsByEmail(email)) {
            String initialPass = (request.getInitialPassword() != null && !request.getInitialPassword().isBlank())
                    ? request.getInitialPassword()
                    : "School@" + (1000 + new Random().nextInt(9000));

            user = User.builder()
                    .school(school)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(initialPass))
                    .firstName(request.getFirstName().trim())
                    .lastName(request.getLastName().trim())
                    .phone(request.getPhone())
                    .build();
            user = userRepository.save(user);
        } else {
            user = userRepository.findByEmail(email).orElse(null);
        }

        // Optional Class & Section allocation
        SchoolClass assignedClass = null;
        if (request.getAssignedClassId() != null) {
            assignedClass = classRepository.findByIdAndSchoolId(request.getAssignedClassId(), schoolId).orElse(null);
        }

        Section assignedSection = null;
        if (request.getAssignedSectionId() != null) {
            assignedSection = sectionRepository.findByIdAndSchoolId(request.getAssignedSectionId(), schoolId).orElse(null);
        }

        Staff staff = Staff.builder()
                .school(school)
                .user(user)
                .employeeId(empId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(email)
                .phone(request.getPhone().trim())
                .role(request.getRole() != null ? request.getRole() : "TEACHER")
                .designation(request.getDesignation())
                .department(request.getDepartment())
                .assignedClass(assignedClass)
                .assignedSection(assignedSection)
                .qualification(request.getQualification())
                .dateOfJoining(request.getDateOfJoining())
                .status("ACTIVE")
                .build();

        Staff saved = staffRepository.save(staff);

        auditService.log("STAFF_CREATED", "staff", saved.getId(),
                "Added staff: " + saved.getFirstName() + " " + saved.getLastName() + " (" + saved.getEmployeeId() + ")");

        return toDto(saved);
    }

    // ── Update Staff ──────────────────────────────────────────────────
    @Transactional
    public StaffDto updateStaff(Long id, CreateStaffRequest request) {
        Long schoolId = TenantContext.requireSchoolId();

        Staff staff = staffRepository.findBySchoolIdAndId(schoolId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with ID: " + id));

        if (request.getFirstName() != null) staff.setFirstName(request.getFirstName().trim());
        if (request.getLastName() != null) staff.setLastName(request.getLastName().trim());
        if (request.getPhone() != null) staff.setPhone(request.getPhone().trim());
        if (request.getDesignation() != null) staff.setDesignation(request.getDesignation().trim());
        if (request.getDepartment() != null) staff.setDepartment(request.getDepartment().trim());
        if (request.getRole() != null) staff.setRole(request.getRole());
        if (request.getQualification() != null) staff.setQualification(request.getQualification());

        if (request.getAssignedClassId() != null) {
            staff.setAssignedClass(classRepository.findByIdAndSchoolId(request.getAssignedClassId(), schoolId).orElse(null));
        }
        if (request.getAssignedSectionId() != null) {
            staff.setAssignedSection(sectionRepository.findByIdAndSchoolId(request.getAssignedSectionId(), schoolId).orElse(null));
        }

        Staff updated = staffRepository.save(staff);
        return toDto(updated);
    }

    // ── Reset Credentials ─────────────────────────────────────────────
    @Transactional
    public Map<String, String> resetCredentials(Long id) {
        Long schoolId = TenantContext.requireSchoolId();

        Staff staff = staffRepository.findBySchoolIdAndId(schoolId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found with ID: " + id));

        String newPassword = "Reset@" + (1000 + new Random().nextInt(9000));

        if (staff.getUser() != null) {
            User user = staff.getUser();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
        } else {
            // Provision user if not existing
            User user = User.builder()
                    .school(staff.getSchool())
                    .email(staff.getEmail())
                    .passwordHash(passwordEncoder.encode(newPassword))
                    .firstName(staff.getFirstName())
                    .lastName(staff.getLastName())
                    .phone(staff.getPhone())
                    .build();
            user = userRepository.save(user);
            staff.setUser(user);
            staffRepository.save(staff);
        }

        auditService.log("STAFF_PASSWORD_RESET", "staff", staff.getId(),
                "Issued temporary credentials for staff: " + staff.getEmail());

        Map<String, String> response = new HashMap<>();
        response.put("temporaryPassword", newPassword);
        response.put("message", "Temporary password generated successfully.");
        return response;
    }

    // ── Mapping Helper ────────────────────────────────────────────────
    private StaffDto toDto(Staff s) {
        return StaffDto.builder()
                .id(s.getId())
                .employeeId(s.getEmployeeId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .fullName(s.getFirstName() + " " + s.getLastName())
                .email(s.getEmail())
                .phone(s.getPhone())
                .role(s.getRole())
                .designation(s.getDesignation())
                .department(s.getDepartment())
                .assignedClassId(s.getAssignedClass() != null ? s.getAssignedClass().getId() : null)
                .assignedClassName(s.getAssignedClass() != null ? s.getAssignedClass().getName() : null)
                .assignedSectionId(s.getAssignedSection() != null ? s.getAssignedSection().getId() : null)
                .assignedSectionName(s.getAssignedSection() != null ? s.getAssignedSection().getName() : null)
                .subjectsTaught(Collections.emptyList())
                .qualification(s.getQualification())
                .dateOfJoining(s.getDateOfJoining())
                .status(s.getStatus())
                .build();
    }
}
