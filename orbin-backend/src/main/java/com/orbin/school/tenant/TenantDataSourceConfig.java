package com.orbin.school.tenant;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Enforces PostgreSQL Row-Level Security (RLS) on all database connections.
 * Sets PostgreSQL session variables:
 * - app.current_school_id
 * - app.is_platform_admin
 * And guarantees cleanup on connection release.
 */
@Slf4j
@Configuration
public class TenantDataSourceConfig implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof DataSource ds && !(bean instanceof TenantAwareDataSource)) {
            log.info("Wrapping DataSource with TenantAwareDataSource for PostgreSQL RLS enforcement");
            return new TenantAwareDataSource(ds);
        }
        return bean;
    }

    public static class TenantAwareDataSource extends DelegatingDataSource {

        public TenantAwareDataSource(DataSource targetDataSource) {
            super(targetDataSource);
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection conn = super.getConnection();
            return wrapConnection(conn);
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            Connection conn = super.getConnection(username, password);
            return wrapConnection(conn);
        }

        private Connection wrapConnection(Connection conn) {
            Long schoolId = TenantContext.getSchoolId();
            boolean isPlatformAdmin = checkIsPlatformAdmin();

            try (Statement stmt = conn.createStatement()) {
                if (schoolId != null) {
                    stmt.execute("SELECT set_config('app.current_school_id', '" + schoolId + "', false)");
                } else {
                    stmt.execute("SELECT set_config('app.current_school_id', '', false)");
                }
                if (isPlatformAdmin) {
                    stmt.execute("SELECT set_config('app.is_platform_admin', 'true', false)");
                } else {
                    stmt.execute("SELECT set_config('app.is_platform_admin', 'false', false)");
                }
            } catch (SQLException e) {
                log.debug("DB session config check: {}", e.getMessage());
            }

            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> {
                        if ("close".equals(method.getName())) {
                            try {
                                if (!conn.isClosed()) {
                                    try (Statement stmt = conn.createStatement()) {
                                        stmt.execute("SELECT set_config('app.current_school_id', '', false), set_config('app.is_platform_admin', 'false', false)");
                                    } catch (SQLException ignored) {}
                                }
                            } finally {
                                return method.invoke(conn, args);
                            }
                        }
                        return method.invoke(conn, args);
                    }
            );
        }

        private boolean checkIsPlatformAdmin() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ORBIN_ADMIN".equals(a.getAuthority()) || "ORBIN_ADMIN".equals(a.getAuthority()));
        }
    }
}
