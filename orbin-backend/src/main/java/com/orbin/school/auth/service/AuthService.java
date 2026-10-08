package com.orbin.school.auth.service;

import com.orbin.school.auth.dto.AuthResponse;
import com.orbin.school.auth.dto.LoginRequest;
import com.orbin.school.auth.dto.RefreshRequest;
import com.orbin.school.auth.entity.RefreshToken;
import com.orbin.school.auth.repository.RefreshTokenRepository;
import com.orbin.school.common.JwtProperties;
import com.orbin.school.common.exception.BusinessException;
import com.orbin.school.common.exception.ResourceNotFoundException;
import com.orbin.school.security.JwtService;
import com.orbin.school.tenant.TenantContext;
import com.orbin.school.user.entity.User;
import com.orbin.school.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Handles login, token refresh, and logout.
 *
 * <p>Refresh token rotation: on every refresh, the old token is revoked
 * and a new token with the same family is issued.
 *
 * <p>Theft detection: if a revoked token in a family is presented, all
 * tokens in that family are revoked (session invalidated).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager    authenticationManager;
    private final JwtService               jwtService;
    private final RefreshTokenRepository   refreshTokenRepository;
    private final UserRepository           userRepository;
    private final JwtProperties            jwtProperties;

    // ── Login ─────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = (User) auth.getPrincipal();
        String accessToken  = jwtService.generateAccessToken(user);
        String refreshToken = createRefreshToken(user, UUID.randomUUID().toString(), httpRequest);

        log.info("User {} logged in from {}", user.getEmail(), httpRequest.getRemoteAddr());

        return buildResponse(user, accessToken, refreshToken);
    }

    // ── Refresh ───────────────────────────────────────────────────────

    @Transactional
    public AuthResponse refresh(RefreshRequest request, HttpServletRequest httpRequest) {
        RefreshToken existing = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessException("INVALID_TOKEN", "Invalid refresh token"));

        // Theft detection: if already revoked, revoke entire family
        if (existing.isRevoked()) {
            log.warn("Revoked refresh token reuse detected — revoking family {} for user {}",
                    existing.getFamily(), existing.getUser().getId());
            refreshTokenRepository.revokeAllByFamily(existing.getFamily(), Instant.now());
            throw new BusinessException("TOKEN_REUSE_DETECTED",
                    "Session invalidated due to suspicious activity. Please log in again.");
        }

        if (existing.isExpired()) {
            throw new BusinessException("TOKEN_EXPIRED", "Refresh token expired. Please log in again.");
        }

        // Rotate: revoke old, issue new
        existing.revoke();
        refreshTokenRepository.save(existing);

        User user = existing.getUser();
        String newAccessToken  = jwtService.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user, existing.getFamily(), httpRequest);

        return buildResponse(user, newAccessToken, newRefreshToken);
    }

    // ── Logout ────────────────────────────────────────────────────────

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(rt -> {
            rt.revoke();
            refreshTokenRepository.save(rt);
            log.info("User {} logged out", rt.getUser().getEmail());
        });
    }

    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, Instant.now());
        log.info("User id={} logged out of all sessions", userId);
    }

    @Transactional(readOnly = true)
    public AuthResponse getMe() {
        Long userId = TenantContext.requireUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return buildResponse(user, "", "");
    }

    // ── Internal ──────────────────────────────────────────────────────

    private String createRefreshToken(User user, String family, HttpServletRequest request) {
        String tokenValue = UUID.randomUUID().toString() + "-" + UUID.randomUUID();
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .family(family)
                .expiresAt(Instant.now().plusMillis(jwtProperties.getRefreshTokenExpirationMs()))
                .userAgent(truncate(request.getHeader("User-Agent"), 300))
                .ipAddress(request.getRemoteAddr())
                .build();
        refreshTokenRepository.save(rt);
        return tokenValue;
    }

    private AuthResponse buildResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream().map(r -> r.getName()).toList();
        Long schoolId = user.getSchool() != null ? user.getSchool().getId() : null;
        return AuthResponse.of(accessToken, refreshToken,
                jwtProperties.getAccessTokenExpirationMs(),
                user.getId(), schoolId, user.getEmail(), user.getFullName(), roles);
    }

    private String truncate(String value, int maxLen) {
        if (value == null) return null;
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}
