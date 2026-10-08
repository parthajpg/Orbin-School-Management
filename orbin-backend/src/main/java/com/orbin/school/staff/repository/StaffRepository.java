package com.orbin.school.staff.repository;

import com.orbin.school.staff.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Long> {

    List<Staff> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

    Optional<Staff> findBySchoolIdAndId(Long schoolId, Long id);

    Optional<Staff> findBySchoolIdAndEmail(Long schoolId, String email);

    Optional<Staff> findBySchoolIdAndEmployeeId(Long schoolId, String employeeId);

    boolean existsBySchoolIdAndEmail(Long schoolId, String email);

    boolean existsBySchoolIdAndEmployeeId(Long schoolId, String employeeId);
}
