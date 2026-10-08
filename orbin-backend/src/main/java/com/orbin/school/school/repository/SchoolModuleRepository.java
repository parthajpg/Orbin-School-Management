package com.orbin.school.school.repository;

import com.orbin.school.school.entity.SchoolModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchoolModuleRepository extends JpaRepository<SchoolModule, Long> {
    List<SchoolModule> findBySchoolId(Long schoolId);
}
