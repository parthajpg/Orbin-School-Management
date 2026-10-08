package com.orbin.school.parent.repository;

import com.orbin.school.parent.entity.ParentStudent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParentStudentRepository extends JpaRepository<ParentStudent, Long> {
    List<ParentStudent> findByParentId(Long parentId);
    List<ParentStudent> findByStudentId(Long studentId);
    Optional<ParentStudent> findByParentIdAndStudentId(Long parentId, Long studentId);

    @Query("SELECT ps FROM ParentStudent ps WHERE ps.parent.normalizedPhone = :phone")
    List<ParentStudent> findByParentNormalizedPhone(@Param("phone") String phone);
}
