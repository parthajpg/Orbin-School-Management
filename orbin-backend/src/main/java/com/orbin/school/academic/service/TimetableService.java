package com.orbin.school.academic.service;

import com.orbin.school.academic.dto.*;
import com.orbin.school.academic.entity.*;
import com.orbin.school.academic.repository.*;
import com.orbin.school.audit.service.AuditService;
import com.orbin.school.common.exception.DuplicateResourceException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.school.entity.School;
import com.orbin.school.school.repository.SchoolRepository;
import com.orbin.school.staff.entity.Staff;
import com.orbin.school.staff.repository.StaffRepository;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import com.orbin.school.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimetableService {

    private final PeriodSlotRepository       periodSlotRepository;
    private final TimetableEntryRepository   timetableEntryRepository;
    private final AcademicYearRepository     academicYearRepository;
    private final SchoolClassRepository      schoolClassRepository;
    private final SectionRepository          sectionRepository;
    private final SubjectRepository          subjectRepository;
    private final UserRepository             userRepository;
    private final StaffRepository            staffRepository;
    private final SchoolRepository           schoolRepository;
    private final AuditService               auditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Period Slots (Bell Schedule)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PeriodSlotDto> getPeriodSlots() {
        Long schoolId = TenantContext.requireSchoolId();
        return periodSlotRepository.findBySchoolIdOrderBySlotNumberAsc(schoolId)
                .stream()
                .map(this::toSlotDto)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Timetable Entries
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableForSection(Long sectionId) {
        Long schoolId = TenantContext.requireSchoolId();
        return timetableEntryRepository.findBySchoolIdAndSectionIdOrderByDayOfWeekAscPeriodSlotSlotNumberAsc(schoolId, sectionId)
                .stream()
                .map(this::toEntryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getTimetableForTeacher(Long teacherId, Integer dayOfWeek) {
        Long schoolId = TenantContext.requireSchoolId();
        List<TimetableEntry> entries = (dayOfWeek != null)
                ? timetableEntryRepository.findBySchoolIdAndTeacherIdAndDayOfWeekOrderByPeriodSlotSlotNumberAsc(schoolId, teacherId, dayOfWeek)
                : timetableEntryRepository.findBySchoolIdAndTeacherIdOrderByDayOfWeekAscPeriodSlotSlotNumberAsc(schoolId, teacherId);

        return entries.stream().map(this::toEntryDto).toList();
    }

    @Transactional(readOnly = true)
    public List<TimetableEntryDto> getMyScheduleToday() {
        Long schoolId = TenantContext.requireSchoolId();
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return Collections.emptyList();
        }

        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue(); // 1=Monday .. 7=Sunday
        return timetableEntryRepository.findBySchoolIdAndTeacherIdAndDayOfWeekOrderByPeriodSlotSlotNumberAsc(schoolId, currentUser.getId(), dayOfWeek)
                .stream()
                .map(this::toEntryDto)
                .toList();
    }

    @Transactional
    public TimetableEntryDto createTimetableEntry(CreateTimetableEntryRequest req) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        AcademicYear ay = (req.academicYearId() != null)
                ? academicYearRepository.findByIdAndSchoolId(req.academicYearId(), schoolId)
                        .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", req.academicYearId()))
                : academicYearRepository.findBySchoolIdAndCurrentTrue(schoolId)
                        .orElseGet(() -> academicYearRepository.findBySchoolId(schoolId).stream().findFirst()
                                .orElseThrow(() -> new ResourceNotFoundException("No active AcademicYear found")));

        Section section = sectionRepository.findByIdAndSchoolId(req.sectionId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Section", req.sectionId()));

        PeriodSlot slot = periodSlotRepository.findByIdAndSchoolId(req.periodSlotId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("PeriodSlot", req.periodSlotId()));

        User teacher = userRepository.findByIdAndSchoolId(req.teacherId(), schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Teacher", req.teacherId()));

        // Conflict check 1: Teacher collision
        Optional<TimetableEntry> teacherCollision = timetableEntryRepository
                .findBySchoolIdAndAcademicYearIdAndTeacherIdAndPeriodSlotIdAndDayOfWeek(
                        schoolId, ay.getId(), teacher.getId(), slot.getId(), req.dayOfWeek());
        if (teacherCollision.isPresent() && !teacherCollision.get().getSection().getId().equals(section.getId())) {
            throw new DuplicateResourceException("Teacher " + teacher.getFullName() + " is already assigned to " +
                    teacherCollision.get().getSection().getSchoolClass().getName() + " - " +
                    teacherCollision.get().getSection().getName() + " during " + slot.getName() + " on " + getDayName(req.dayOfWeek()));
        }

        // Conflict check 2: Section already has class in this slot
        Optional<TimetableEntry> sectionExisting = timetableEntryRepository
                .findBySchoolIdAndAcademicYearIdAndSectionIdAndPeriodSlotIdAndDayOfWeek(
                        schoolId, ay.getId(), section.getId(), slot.getId(), req.dayOfWeek());

        Subject subject = null;
        if (req.subjectId() != null) {
            subject = subjectRepository.findByIdAndSchoolId(req.subjectId(), schoolId).orElse(null);
        }

        TimetableEntry entry = sectionExisting.orElseGet(() -> TimetableEntry.builder()
                .school(school)
                .academicYear(ay)
                .section(section)
                .periodSlot(slot)
                .dayOfWeek(req.dayOfWeek())
                .build());

        entry.setTeacher(teacher);
        entry.setSubject(subject);
        entry.setRoomNumber(req.roomNumber());
        entry.setIsSubstitution(false);
        entry.setOriginalTeacher(null);

        TimetableEntry saved = timetableEntryRepository.save(entry);
        auditService.log("TIMETABLE_SET", "TimetableEntry", saved.getId(),
                "Scheduled " + (subject != null ? subject.getName() : "Homeroom") + " for " +
                section.getSchoolClass().getName() + " " + section.getName() + " on " + getDayName(req.dayOfWeek()) + " slot " + slot.getName());

        return toEntryDto(saved);
    }

    @Transactional
    public TimetableEntryDto assignSubstitute(Long entryId, Long substituteTeacherId) {
        Long schoolId = TenantContext.requireSchoolId();

        TimetableEntry entry = timetableEntryRepository.findByIdAndSchoolId(entryId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableEntry", entryId));

        User substitute = userRepository.findByIdAndSchoolId(substituteTeacherId, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("Substitute Teacher", substituteTeacherId));

        if (!entry.getIsSubstitution()) {
            entry.setOriginalTeacher(entry.getTeacher());
        }
        entry.setTeacher(substitute);
        entry.setIsSubstitution(true);

        TimetableEntry saved = timetableEntryRepository.save(entry);
        auditService.log("TIMETABLE_SUBSTITUTE", "TimetableEntry", saved.getId(),
                "Assigned proxy teacher " + substitute.getFullName() + " for original teacher " +
                (entry.getOriginalTeacher() != null ? entry.getOriginalTeacher().getFullName() : "N/A"));

        return toEntryDto(saved);
    }

    @Transactional
    public void deleteTimetableEntry(Long id) {
        Long schoolId = TenantContext.requireSchoolId();
        TimetableEntry entry = timetableEntryRepository.findByIdAndSchoolId(id, schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("TimetableEntry", id));
        timetableEntryRepository.delete(entry);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bulk Timetable CSV / Spreadsheet Ingestion
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public TimetableValidationResult processBulkImport(List<TimetableImportRow> rows, boolean commit) {
        Long schoolId = TenantContext.requireSchoolId();
        School school = schoolRepository.getReferenceById(schoolId);

        AcademicYear ay = academicYearRepository.findBySchoolIdAndCurrentTrue(schoolId)
                .orElseGet(() -> academicYearRepository.findBySchoolId(schoolId).stream().findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("No active AcademicYear found")));

        List<PeriodSlot> allSlots = periodSlotRepository.findBySchoolIdOrderBySlotNumberAsc(schoolId);
        Map<Integer, PeriodSlot> slotMap = new HashMap<>();
        for (PeriodSlot s : allSlots) {
            slotMap.put(s.getSlotNumber(), s);
        }

        List<String> errors = new ArrayList<>();
        List<TimetableEntryDto> previewEntries = new ArrayList<>();
        int validRows = 0;

        // In-memory teacher collision tracker for batch: key = "teacherId_slotNumber_day"
        Set<String> teacherBusyKeys = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            TimetableImportRow row = rows.get(i);
            int rowNum = i + 1;

            if (row.className() == null || row.className().isBlank()) {
                errors.add("Row " + rowNum + ": Class name is missing.");
                continue;
            }
            if (row.sectionName() == null || row.sectionName().isBlank()) {
                errors.add("Row " + rowNum + ": Section name is missing.");
                continue;
            }
            if (row.periodNumber() == null) {
                errors.add("Row " + rowNum + ": Period number is missing.");
                continue;
            }

            PeriodSlot slot = slotMap.get(row.periodNumber());
            if (slot == null) {
                errors.add("Row " + rowNum + ": Period slot " + row.periodNumber() + " not found in school bell schedule.");
                continue;
            }

            int dayOfWeek = parseDayOfWeek(row.day());
            if (dayOfWeek < 1 || dayOfWeek > 7) {
                errors.add("Row " + rowNum + ": Invalid day '" + row.day() + "'. Use Monday..Saturday or 1..6.");
                continue;
            }

            // Find Class
            String cleanClassName = row.className().trim();
            SchoolClass sc = schoolClassRepository.findBySchoolIdOrderByDisplayOrderAsc(schoolId).stream()
                    .filter(c -> c.getName().equalsIgnoreCase(cleanClassName))
                    .findFirst()
                    .orElse(null);

            if (sc == null) {
                errors.add("Row " + rowNum + ": Class '" + cleanClassName + "' not found in school.");
                continue;
            }

            // Find Section
            String cleanSecName = row.sectionName().trim();
            Section section = sectionRepository.findBySchoolIdAndSchoolClassId(schoolId, sc.getId()).stream()
                    .filter(sec -> sec.getName().equalsIgnoreCase(cleanSecName))
                    .findFirst()
                    .orElse(null);

            if (section == null) {
                errors.add("Row " + rowNum + ": Section '" + cleanSecName + "' not found under " + cleanClassName + ".");
                continue;
            }

            // Find Teacher
            User teacher = null;
            if (row.teacherEmail() != null && !row.teacherEmail().isBlank()) {
                String teacherKey = row.teacherEmail().trim();
                teacher = userRepository.findByEmail(teacherKey).orElse(null);

                if (teacher == null) {
                    // Try looking up via staff employee ID
                    Staff staff = staffRepository.findBySchoolIdAndEmployeeId(schoolId, teacherKey).orElse(null);
                    if (staff != null) {
                        teacher = staff.getUser();
                    }
                }
            }

            if (teacher == null) {
                // If homeroom and section has classTeacher, fall back to classTeacher
                if (section.getClassTeacher() != null) {
                    teacher = section.getClassTeacher();
                } else {
                    errors.add("Row " + rowNum + ": Teacher '" + row.teacherEmail() + "' could not be resolved.");
                    continue;
                }
            }

            // Check collision within batch
            String busyKey = teacher.getId() + "_" + slot.getId() + "_" + dayOfWeek;
            if (teacherBusyKeys.contains(busyKey)) {
                errors.add("Row " + rowNum + ": Collision! Teacher " + teacher.getFullName() +
                        " is already scheduled for Period " + slot.getSlotNumber() + " on " + getDayName(dayOfWeek) + " earlier in this sheet.");
                continue;
            }
            teacherBusyKeys.add(busyKey);

            // Find Subject (optional for Nursery/Primary, required or optional for others)
            Subject subject = null;
            if (row.subjectCode() != null && !row.subjectCode().isBlank()) {
                String subQuery = row.subjectCode().trim();
                subject = subjectRepository.findBySchoolIdAndSchoolClassId(schoolId, sc.getId()).stream()
                        .filter(s -> (s.getCode() != null && s.getCode().equalsIgnoreCase(subQuery)) || s.getName().equalsIgnoreCase(subQuery))
                        .findFirst()
                        .orElse(null);
            }

            validRows++;

            if (commit) {
                // Upsert into DB
                Optional<TimetableEntry> existing = timetableEntryRepository
                        .findBySchoolIdAndAcademicYearIdAndSectionIdAndPeriodSlotIdAndDayOfWeek(
                                schoolId, ay.getId(), section.getId(), slot.getId(), dayOfWeek);

                TimetableEntry entry = existing.orElseGet(() -> TimetableEntry.builder()
                        .school(school)
                        .academicYear(ay)
                        .section(section)
                        .periodSlot(slot)
                        .dayOfWeek(dayOfWeek)
                        .build());

                entry.setTeacher(teacher);
                entry.setSubject(subject);
                entry.setRoomNumber(row.roomNumber());
                entry.setIsSubstitution(false);

                TimetableEntry saved = timetableEntryRepository.save(entry);
                previewEntries.add(toEntryDto(saved));
            } else {
                // Build preview DTO
                previewEntries.add(new TimetableEntryDto(
                        null,
                        ay.getId(),
                        section.getId(),
                        sc.getName(),
                        section.getName(),
                        slot.getId(),
                        slot.getSlotNumber(),
                        slot.getName(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        slot.getIsBreak(),
                        dayOfWeek,
                        getDayName(dayOfWeek),
                        teacher.getId(),
                        teacher.getFullName(),
                        teacher.getEmail(),
                        subject != null ? subject.getId() : null,
                        subject != null ? subject.getName() : "Homeroom",
                        subject != null ? subject.getCode() : "HOMEROOM",
                        row.roomNumber(),
                        false,
                        null,
                        null
                ));
            }
        }

        boolean canCommit = errors.isEmpty() && validRows > 0;
        return new TimetableValidationResult(rows.size(), validRows, errors.size(), errors, previewEntries, canCommit);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper Methods & Mappers
    // ─────────────────────────────────────────────────────────────────────────

    private int parseDayOfWeek(String day) {
        if (day == null) return -1;
        String clean = day.trim().toLowerCase();
        try {
            int num = Integer.parseInt(clean);
            return (num >= 1 && num <= 7) ? num : -1;
        } catch (NumberFormatException ignored) {}

        return switch (clean) {
            case "monday", "mon" -> 1;
            case "tuesday", "tue" -> 2;
            case "wednesday", "wed" -> 3;
            case "thursday", "thu" -> 4;
            case "friday", "fri" -> 5;
            case "saturday", "sat" -> 6;
            case "sunday", "sun" -> 7;
            default -> -1;
        };
    }

    private String getDayName(int dayOfWeek) {
        return switch (dayOfWeek) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "Unknown";
        };
    }

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private PeriodSlotDto toSlotDto(PeriodSlot s) {
        return new PeriodSlotDto(
                s.getId(),
                s.getSlotNumber(),
                s.getName(),
                s.getStartTime(),
                s.getEndTime(),
                s.getIsBreak(),
                s.getTier()
        );
    }

    private TimetableEntryDto toEntryDto(TimetableEntry te) {
        return new TimetableEntryDto(
                te.getId(),
                te.getAcademicYear().getId(),
                te.getSection().getId(),
                te.getSection().getSchoolClass().getName(),
                te.getSection().getName(),
                te.getPeriodSlot().getId(),
                te.getPeriodSlot().getSlotNumber(),
                te.getPeriodSlot().getName(),
                te.getPeriodSlot().getStartTime(),
                te.getPeriodSlot().getEndTime(),
                te.getPeriodSlot().getIsBreak(),
                te.getDayOfWeek(),
                getDayName(te.getDayOfWeek()),
                te.getTeacher().getId(),
                te.getTeacher().getFullName(),
                te.getTeacher().getEmail(),
                te.getSubject() != null ? te.getSubject().getId() : null,
                te.getSubject() != null ? te.getSubject().getName() : "Homeroom",
                te.getSubject() != null ? te.getSubject().getCode() : "HOMEROOM",
                te.getRoomNumber(),
                te.getIsSubstitution(),
                te.getOriginalTeacher() != null ? te.getOriginalTeacher().getId() : null,
                te.getOriginalTeacher() != null ? te.getOriginalTeacher().getFullName() : null
        );
    }
}
