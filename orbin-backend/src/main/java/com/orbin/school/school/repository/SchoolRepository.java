package com.orbin.school.school.repository;

import com.orbin.school.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Optional<School> findBySlug(String slug);

    Optional<School> findByCustomDomain(String domain);

    boolean existsBySlug(String slug);

    boolean existsByCustomDomain(String domain);
}
