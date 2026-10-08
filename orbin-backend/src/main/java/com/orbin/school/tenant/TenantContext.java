package com.orbin.school.tenant;

/**
 * Thread-local store for the current authenticated school (tenant).
 *
 * <p>Set by {@link TenantContextFilter} after JWT validation.
 * Cleared after every request to prevent cross-request leakage.
 *
 * <p><strong>CRITICAL:</strong> Business logic must call {@link #requireSchoolId()}
 * rather than trusting any request-supplied schoolId.
 */
public final class TenantContext {

    private TenantContext() {}

    private static final ThreadLocal<Long>   SCHOOL_ID  = new ThreadLocal<>();
    private static final ThreadLocal<Long>   USER_ID    = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_EMAIL = new ThreadLocal<>();

    // ── Write ─────────────────────────────────────────────────────────

    public static void setSchoolId(Long schoolId) {
        SCHOOL_ID.set(schoolId);
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static void setUserEmail(String email) {
        USER_EMAIL.set(email);
    }

    // ── Read ──────────────────────────────────────────────────────────

    public static Long getSchoolId() {
        return SCHOOL_ID.get();
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static String getUserEmail() {
        return USER_EMAIL.get();
    }

    /**
     * Returns the school ID or throws if not set.
     * Use this in every service method that requires tenant isolation.
     */
    public static Long requireSchoolId() {
        Long id = SCHOOL_ID.get();
        if (id == null) {
            throw new IllegalStateException(
                    "TenantContext schoolId is null — unauthenticated access to tenant-scoped operation");
        }
        return id;
    }

    /**
     * Returns the user ID or throws if not set.
     */
    public static Long requireUserId() {
        Long id = USER_ID.get();
        if (id == null) {
            throw new IllegalStateException(
                    "TenantContext userId is null — unauthenticated access");
        }
        return id;
    }

    // ── Cleanup ───────────────────────────────────────────────────────

    /**
     * Must be called at the end of every request to prevent thread-pool leakage.
     * Called by {@link TenantContextFilter}.
     */
    public static void clear() {
        SCHOOL_ID.remove();
        USER_ID.remove();
        USER_EMAIL.remove();
    }
}
