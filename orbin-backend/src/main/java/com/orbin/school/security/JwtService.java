package com.orbin.school.security;

import com.orbin.school.common.JwtProperties;
import com.orbin.school.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.function.Function;

/**
 * Issues and validates JWT access tokens.
 *
 * <p>Claims embedded in access token:
 * <ul>
 *   <li>{@code sub} – user email</li>
 *   <li>{@code uid} – userId (Long)</li>
 *   <li>{@code sid} – schoolId (Long, null for platform admins)</li>
 *   <li>{@code roles} – list of role names</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    // ── Token Generation ──────────────────────────────────────────────

    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("uid", user.getId());
        claims.put("sid", user.getSchool() != null ? user.getSchool().getId() : null);
        claims.put("roles", user.getRoles().stream()
                .map(r -> r.getName())
                .toList());
        return buildToken(claims, user.getEmail(), jwtProperties.getAccessTokenExpirationMs());
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Token Validation ──────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ── Claim Extraction ──────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Object uid = parseClaims(token).get("uid");
        return uid != null ? Long.valueOf(uid.toString()) : null;
    }

    public Long extractSchoolId(String token) {
        Object sid = parseClaims(token).get("sid");
        return sid != null ? Long.valueOf(sid.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parseClaims(token).get("roles");
        return roles instanceof List<?> list ? (List<String>) list : Collections.emptyList();
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseClaims(token));
    }

    // ── Internal ──────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                Base64.getEncoder().encodeToString(
                        jwtProperties.getSecret().getBytes()));
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
