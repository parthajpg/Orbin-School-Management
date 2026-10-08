-- ==========================================================================
-- V2__seed_permissions_and_roles.sql
-- Seeds all platform permissions and built-in system roles
-- ==========================================================================

-- ── Permissions ───────────────────────────────────────────────────────────
INSERT INTO permissions (name, description, module) VALUES
-- Students
('students.read',           'View student records',              'Students'),
('students.create',         'Admit new students',                'Students'),
('students.update',         'Update student records',            'Students'),
('students.delete',         'Archive/remove students',           'Students'),
-- Parents
('parents.read',            'View parent/guardian records',      'Parents'),
('parents.manage',          'Create and update parents',         'Parents'),
-- Staff / Users
('users.read',              'View staff list',                   'Users'),
('users.manage',            'Create and manage school users',    'Users'),
('roles.manage',            'Assign roles and permissions',      'Roles'),
-- Attendance
('attendance.read',         'View attendance records',           'Attendance'),
('attendance.write',        'Mark and edit attendance',          'Attendance'),
-- Fees
('fees.read',               'View fee records',                  'Fees'),
('fees.write',              'Create and edit fee records',       'Fees'),
('fees.manage',             'Full fee management + receipts',    'Fees'),
('payments.record',         'Record payments',                   'Fees'),
-- Academic
('academic.read',           'View academic structure',           'Academic'),
('academic.manage',         'Manage classes, sections, subjects','Academic'),
-- Syllabus
('syllabus.read',           'View syllabus',                     'Syllabus'),
('syllabus.write',          'Update syllabus progress',          'Syllabus'),
-- Homework
('homework.read',           'View homework',                     'Homework'),
('homework.write',          'Create and publish homework',       'Homework'),
-- Materials
('materials.read',          'View learning materials',           'Materials'),
('materials.write',         'Upload learning materials',         'Materials'),
-- Tests
('tests.read',              'View tests',                        'Tests'),
('tests.manage',            'Create and manage tests',           'Tests'),
-- Results
('results.read',            'View results',                      'Results'),
('results.manage',          'Enter and edit results',            'Results'),
-- Achievements
('achievements.read',       'View achievements',                 'Achievements'),
('achievements.write',      'Award achievements',                'Achievements'),
-- Events
('events.read',             'View events',                       'Events'),
('events.manage',           'Create and manage events',          'Events'),
-- Announcements
('announcements.read',      'View announcements',                'Announcements'),
('announcements.write',     'Create announcements',              'Announcements'),
-- Website
('website.view',            'View website CMS',                  'Website'),
('website.edit',            'Edit school website content',       'Website'),
-- Reports
('reports.view',            'View school reports',               'Reports'),
-- Notifications
('notifications.send',      'Send WhatsApp/notification blasts', 'Notifications'),
-- Audit
('audit.view',              'View audit logs',                   'Audit'),
-- Platform (no school_id – for ORBIN_ADMIN only)
('platform.schools.manage', 'Create and manage school tenants',  'Platform'),
('platform.modules.manage', 'Configure modules for schools',     'Platform'),
('platform.audit.view',     'View platform-wide audit logs',     'Platform')
ON CONFLICT (name) DO NOTHING;

-- ── Roles (system roles: school_id IS NULL means platform-level) ──────────

-- Orbin Platform Admin (no school)
INSERT INTO roles (school_id, name, description, is_system_role)
VALUES (NULL, 'ORBIN_ADMIN', 'Orbin platform administrator', TRUE)
ON CONFLICT DO NOTHING;

-- Note: school-specific roles (PRINCIPAL, TEACHER, etc.) are created
-- during school onboarding via V3__seed_school_roles.sql or dynamically.
