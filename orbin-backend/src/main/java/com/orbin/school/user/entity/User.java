package com.orbin.school.user.entity;

import com.orbin.school.common.entity.BaseEntity;
import com.orbin.school.role.entity.Role;
import com.orbin.school.school.entity.School;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Platform user entity.
 *
 * <p>A user belongs to exactly ONE school (or no school for platform admins).
 * This is intentional — users cannot switch schools.
 *
 * <p>Implements {@link UserDetails} for Spring Security integration.
 */
@Entity
@Table(name = "users",
       indexes = {
           @Index(name = "idx_users_email",     columnList = "email"),
           @Index(name = "idx_users_school_id", columnList = "school_id")
       })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class User extends BaseEntity implements UserDetails {

    /** Null for ORBIN_ADMIN platform users. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default
    private boolean forcePasswordChange = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // ── Convenience ───────────────────────────────────────────────────

    public String getFullName() {
        return lastName != null ? firstName + " " + lastName : firstName;
    }

    public boolean hasPermission(String permissionName) {
        return roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .anyMatch(p -> p.getName().equals(permissionName));
    }

    // ── UserDetails ───────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> authorities = roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                .collect(Collectors.toSet());
        // Also add individual permissions as authorities
        roles.stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> new SimpleGrantedAuthority(p.getName()))
                .forEach(authorities::add);
        return authorities;
    }

    @Override public String getPassword()   { return passwordHash; }
    @Override public String getUsername()   { return email; }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isAccountNonLocked()    { return status != UserStatus.SUSPENDED; }
    @Override public boolean isCredentialsNonExpired(){ return true; }
    @Override public boolean isEnabled()             { return status == UserStatus.ACTIVE; }

    public enum UserStatus {
        ACTIVE, SUSPENDED, PENDING_VERIFICATION, ARCHIVED
    }
}
