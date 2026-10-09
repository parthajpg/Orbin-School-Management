package com.orbin.school.academic.repository;

import com.orbin.school.academic.entity.TimetableEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TimetableEntryRepository extends JpaRepository<TimetableEntry, Long> {

    List<TimetableEntry> findBySchoolIdAndSectionIdOrderByDayOfWeekAscPeriodSlotSlotNumberAsc(Long schoolId, Long sectionId);

    List<TimetableEntry> findBySchoolIdAndTeacherIdOrderByDayOfWeekAscPeriodSlotSlotNumberAsc(Long schoolId, Long teacherId);

    List<TimetableEntry> findBySchoolIdAndTeacherIdAndDayOfWeekOrderByPeriodSlotSlotNumberAsc(Long schoolId, Long teacherId, Integer dayOfWeek);

    Optional<TimetableEntry> findByIdAndSchoolId(Long id, Long schoolId);

    Optional<TimetableEntry> findBySchoolIdAndAcademicYearIdAndSectionIdAndPeriodSlotIdAndDayOfWeek(
            Long schoolId, Long academicYearId, Long sectionId, Long periodSlotId, Integer dayOfWeek);

    Optional<TimetableEntry> findBySchoolIdAndAcademicYearIdAndTeacherIdAndPeriodSlotIdAndDayOfWeek(
            Long schoolId, Long academicYearId, Long teacherId, Long periodSlotId, Integer dayOfWeek);

    @Query("SELECT te FROM TimetableEntry te WHERE te.school.id = :schoolId AND te.academicYear.id = :ayId " +
           "AND te.dayOfWeek = :dayOfWeek ORDER BY te.periodSlot.slotNumber ASC")
    List<TimetableEntry> findAllForSchoolDay(
            @Param("schoolId") Long schoolId,
            @Param("ayId") Long ayId,
            @Param("dayOfWeek") Integer dayOfWeek);
}
