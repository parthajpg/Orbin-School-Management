package com.orbin.school.parent.repository;

import com.orbin.school.parent.entity.Parent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByIdAndSchoolId(Long id, Long schoolId);
    List<Parent> findBySchoolId(Long schoolId);
    Optional<Parent> findBySchoolIdAndNormalizedPhone(Long schoolId, String normalizedPhone);
    List<Parent> findByNormalizedPhone(String normalizedPhone);

    @Query("SELECT p FROM Parent p WHERE p.school.id = :schoolId AND " +
           "(LOWER(p.firstName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(p.lastName) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "p.phone LIKE CONCAT('%', :q, '%'))")
    Page<Parent> searchParents(@Param("schoolId") Long schoolId, @Param("q") String query, Pageable pageable);
}
