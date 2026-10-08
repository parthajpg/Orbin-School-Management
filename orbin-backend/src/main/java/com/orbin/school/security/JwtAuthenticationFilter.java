package com.orbin.school.security;

import com.orbin.school.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Intercepts every request, extracts the Bearer JWT, validates it, and:
 * <ol>
 *   <li>Populates {@link SecurityContextHolder}</li>
 *   <li>Populates {@link TenantContext} with schoolId and userId</li>
 *   <li>Sets MDC keys for structured logging</li>
 * </ol>
 * Always clears TenantContext and MDC in the finally block.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = extractBearerToken(request);

            if (StringUtils.hasText(token) && jwtService.isTokenValid(token)) {
                String email    = jwtService.extractEmail(token);
                Long   userId   = jwtService.extractUserId(token);
                Long   schoolId = jwtService.extractSchoolId(token);
                List<String> roles = jwtService.extractRoles(token);

                // Populate TenantContext – single source of truth for tenant isolation
                TenantContext.setUserId(userId);
                TenantContext.setSchoolId(schoolId);
                TenantContext.setUserEmail(email);

                // MDC for structured logs
                MDC.put("userId",   userId   != null ? userId.toString()   : "null");
                MDC.put("schoolId", schoolId != null ? schoolId.toString() : "null");

                // Authenticate in Spring Security if not already done
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userDetailsService.loadUserByUsername(email);
                    var authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            TenantContext.clear();
            MDC.clear();
        }
    }

    private String extractBearerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
