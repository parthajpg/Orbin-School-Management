package com.orbin.school.attendance.service;

import com.orbin.school.academic.entity.Section;
import com.orbin.school.academic.repository.SectionRepository;
import com.orbin.school.attendance.dto.AttendanceRecordDto;
import com.orbin.school.attendance.dto.AttendanceStatsDto;
import com.orbin.school.attendance.dto.MarkAttendanceRequest;
import com.orbin.school.attendance.entity.Attendance;
import com.orbin.school.attendance.repository.AttendanceRepository;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.student.entity.Student;
import com.orbin.school.student.repository.StudentRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import com.orbin.school.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public List<AttendanceRecordDto> markAttendance(MarkAttendanceRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);
        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));

        User currentUser = getCurrentUser();
        List<AttendanceRecordDto> results = new ArrayList<>();

        for (MarkAttendanceRequest.StudentAttendanceEntry entry : req.entries()) {
            Student student = studentRepository.findByIdAndSchoolId(entry.studentId(), schoolId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student", entry.studentId()));

            Attendance attendance = attendanceRepository
                    .findBySchoolIdAndStudentIdAndDate(schoolId, student.getId(), req.date())
                    .orElse(Attendance.builder()
                            .school(school)
                            .student(student)
                            .section(section)
                            .date(req.date())
                            .build());

            Attendance.AttendanceStatus status;
            try {
                status = Attendance.AttendanceStatus.valueOf(entry.status().toUpperCase());
            } catch (Exception e) {
                status = Attendance.AttendanceStatus.PRESENT;
            }

            attendance.setSection(section);
            attendance.setStatus(status);
            attendance.setNotes(entry.notes());
            attendance.setMarkedBy(currentUser);

            Attendance saved = attendanceRepository.save(attendance);
            results.add(toDto(saved));
        }

        auditService.logAsync("MARK_ATTENDANCE", "Section", req.sectionId().toString(), null,
                "Marked " + req.entries().size() + " records on " + req.date());

        return results;
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAttendanceBySectionAndDate(Long sectionId, LocalDate date) {
        Long schoolId = TenantContext.requireSchoolId();
        LocalDate queryDate = date != null ? date : LocalDate.now();
        List<Attendance> records = attendanceRepository.findBySchoolIdAndSectionIdAndDate(schoolId, sectionId, queryDate);
        return records.stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public AttendanceStatsDto getStudentStats(Long studentId, LocalDate from, LocalDate to) {
        Long schoolId = TenantContext.requireSchoolId();
        LocalDate fromDate = from != null ? from : LocalDate.now().minusDays(30);
        LocalDate toDate = to != null ? to : LocalDate.now();

        long present = attendanceRepository.countPresent(schoolId, studentId, fromDate, toDate);
        long total = attendanceRepository.countTotal(schoolId, studentId, fromDate, toDate);
        double percentage = total > 0 ? ((double) present / total) * 100.0 : 0.0;

        return new AttendanceStatsDto(studentId, fromDate, toDate, present, total, Math.round(percentage * 10.0) / 10.0);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordDto> getAbsentStudents(Long sectionId, LocalDate date) {
        Long schoolId = TenantContext.requireSchoolId();
        LocalDate queryDate = date != null ? date : LocalDate.now();
        return attendanceRepository.findAbsentForSection(schoolId, sectionId, queryDate)
                .stream().map(this::toDto).toList();
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private AttendanceRecordDto toDto(Attendance a) {
        return new AttendanceRecordDto(
                a.getId(),
                a.getStudent().getId(),
                a.getStudent().getFullName(),
                a.getStudent().getAdmissionNumber(),
                a.getSection().getId(),
                a.getDate(),
                a.getStatus().name(),
                a.getNotes(),
                a.getMarkedBy() != null ? a.getMarkedBy().getFullName() : null
        );
    }
}
