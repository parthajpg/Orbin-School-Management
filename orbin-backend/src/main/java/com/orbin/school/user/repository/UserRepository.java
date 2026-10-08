package com.orbin.school.user.repository;

import com.orbin.school.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.school.id = :schoolId")
    Optional<User> findByIdAndSchoolId(@Param("id") Long id, @Param("schoolId") Long schoolId);
}
