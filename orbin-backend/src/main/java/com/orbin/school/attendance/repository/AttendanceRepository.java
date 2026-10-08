package com.orbin.school.attendance.repository;

import com.orbin.school.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findBySchoolIdAndStudentIdAndDate(Long schoolId, Long studentId, LocalDate date);

    List<Attendance> findBySchoolIdAndSectionIdAndDate(Long schoolId, Long sectionId, LocalDate date);

    List<Attendance> findBySchoolIdAndStudentIdAndDateBetween(
            Long schoolId, Long studentId, LocalDate from, LocalDate to);

    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.school.id = :schoolId
          AND a.student.id = :studentId
          AND a.date BETWEEN :from AND :to
          AND a.status = 'PRESENT'
        """)
    long countPresent(@Param("schoolId") Long schoolId,
                      @Param("studentId") Long studentId,
                      @Param("from") LocalDate from,
                      @Param("to") LocalDate to);

    @Query("""
        SELECT COUNT(a) FROM Attendance a
        WHERE a.school.id = :schoolId
          AND a.student.id = :studentId
          AND a.date BETWEEN :from AND :to
        """)
    long countTotal(@Param("schoolId") Long schoolId,
                    @Param("studentId") Long studentId,
                    @Param("from") LocalDate from,
                    @Param("to") LocalDate to);

    /** Absent students for a class on a given date — used for WhatsApp notifications */
    @Query("""
        SELECT a FROM Attendance a
        WHERE a.school.id = :schoolId
          AND a.section.id = :sectionId
          AND a.date = :date
          AND a.status = 'ABSENT'
        """)
    List<Attendance> findAbsentForSection(@Param("schoolId") Long schoolId,
                                           @Param("sectionId") Long sectionId,
                                           @Param("date") LocalDate date);
}
