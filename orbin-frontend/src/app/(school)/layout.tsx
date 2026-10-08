'use client';

// ============================================================================
// ORBIN SCHOOL - SCHOOL PORTAL LAYOUT (Principal, Teacher, Accountant)
// ============================================================================

import React, { useState } from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuth } from '@/context/auth-context';
import {
  LayoutDashboard,
  Users,
  UserCheck,
  CalendarCheck2,
  CreditCard,
  BookOpen,
  GraduationCap,
  LogOut,
  School,
  KeyRound,
  X,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';

export default function SchoolLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, currentSchool, logout, changePassword } = useAuth();

  // Change Password Modal State
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [currentPass, setCurrentPass] = useState('');
  const [newPass, setNewPass] = useState('');
  const [confirmPass, setConfirmPass] = useState('');
  const [passwordError, setPasswordError] = useState<string | null>(null);
  const [passwordSuccess, setPasswordSuccess] = useState<string | null>(null);

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  const handleChangePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setPasswordError(null);
    setPasswordSuccess(null);

    if (newPass !== confirmPass) {
      setPasswordError('New password and confirmation do not match.');
      return;
    }

    try {
      const res = await changePassword(currentPass, newPass);
      setPasswordSuccess(res.message);
      setCurrentPass('');
      setNewPass('');
      setConfirmPass('');
      setTimeout(() => {
        setShowPasswordModal(false);
        setPasswordSuccess(null);
      }, 2500);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to update password';
      setPasswordError(msg);
    }
  };

  const navLinks = [
    {
      name: 'Dashboard',
      href: '/dashboard',
      icon: LayoutDashboard,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL']
    },
    {
      name: 'Students & Admissions',
      href: '/students',
      icon: Users,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL']
    },
    {
      name: 'Staff & Faculty',
      href: '/staff',
      icon: UserCheck,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL']
    },
    {
      name: '1-Click Attendance',
      href: '/attendance',
      icon: CalendarCheck2,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL', 'TEACHER']
    },
    {
      name: 'Fees & Receipts',
      href: '/fees',
      icon: CreditCard,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL', 'ACCOUNTANT']
    },
    {
      name: 'Syllabus Tracker',
      href: '/syllabus',
      icon: BookOpen,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL', 'TEACHER']
    },
    {
      name: 'Exams & Gradebook',
      href: '/exams',
      icon: GraduationCap,
      roles: ['SCHOOL_ADMIN', 'PRINCIPAL', 'TEACHER']
    },
  ];

  const visibleNavLinks = navLinks.filter(
    link => !user || link.roles.includes(user.role)
  );

  return (
    <div className="flex h-screen bg-slate-50 overflow-hidden text-slate-800">
      {/* ── Left Sidebar ─────────────────────────────────────────────────── */}
      <aside className="w-64 bg-slate-900 border-r border-slate-800 flex flex-col shrink-0 text-slate-300">
        {/* Brand Header */}
        <div className="h-16 px-5 border-b border-slate-800 flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-blue-600 to-cyan-500 text-white flex items-center justify-center font-bold shadow-md shadow-blue-500/20">
            <School className="w-5 h-5" />
          </div>
          <div className="overflow-hidden">
            <div className="text-sm font-bold text-white tracking-tight truncate">
              {currentSchool?.name || 'Orbin School'}
            </div>
            <div className="text-[11px] text-blue-400 font-medium tracking-wide">
              {currentSchool?.board || 'School Management'}
            </div>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          <div className="px-3 pb-2 text-[10px] font-bold text-slate-500 uppercase tracking-wider">
            {user?.role === 'TEACHER' ? 'Teacher Classdesk' : 'School Administration'}
          </div>

          {visibleNavLinks.map((link) => {
            const Icon = link.icon;
            const isActive = pathname.startsWith(link.href);
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all ${isActive
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-600/25'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/80'
                  }`}
              >
                <Icon className={`w-4 h-4 ${isActive ? 'text-white' : 'text-slate-400'}`} />
                <span>{link.name}</span>
              </Link>
            );
          })}
        </nav>

        {/* Multi-Tenant Security Pill */}
        <div className="p-3 border-t border-slate-800/80">
          <div className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800 text-[11px] flex items-center gap-2">
            <div className="w-2 h-2 rounded-full bg-emerald-500 shadow-sm shadow-emerald-500 animate-pulse" />
            <div className="text-slate-400 truncate">
              Tenant: <span className="text-slate-200 font-mono font-medium">{currentSchool?.slug}.orbin.edu</span>
            </div>
          </div>
        </div>
      </aside>

      {/* ── Main View Area ───────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        {/* Topbar */}
        <header className="h-16 bg-white border-b border-slate-200/80 px-8 flex items-center justify-between shrink-0 shadow-xs z-10">
          <div className="flex items-center gap-3">
            <span className="text-xs font-semibold px-2.5 py-1 rounded-md bg-blue-50 text-blue-700 border border-blue-200/60">
              {currentSchool?.slug}.orbin.edu
            </span>
            <span className="text-xs text-slate-400">&bull;</span>
            <span className="text-xs text-slate-500 font-medium">
              Academic Term 2 (2026-27)
            </span>
          </div>

          {/* User Profile & Actions */}
          <div className="flex items-center gap-4">
            {/* Change Password Trigger Button */}
            <button
              type="button"
              onClick={() => setShowPasswordModal(true)}
              className="px-3 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold flex items-center gap-1.5 transition-all"
              title="Change your account password"
            >
              <KeyRound className="w-3.5 h-3.5 text-blue-600" />
              <span>Change Password</span>
            </button>

            <div className="h-6 w-px bg-slate-200" />

            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-600 text-white font-bold text-xs flex items-center justify-center shadow-xs">
                {user ? `${user.firstName[0]}${user.lastName[0]}` : 'U'}
              </div>
              <div className="hidden sm:block text-left">
                <div className="text-xs font-bold text-slate-900 leading-tight">
                  {user ? `${user.firstName} ${user.lastName}` : 'Administrator'}
                </div>
                <div className="text-[10px] text-slate-500 font-medium">
                  {user?.email}
                </div>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="p-2 rounded-lg text-slate-400 hover:text-red-600 hover:bg-red-50 transition-all"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </header>

        {/* Scrollable View Content */}
        <main className="flex-1 overflow-y-auto p-8 bg-slate-50">
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>

      {/* ── Change Password Modal ────────────────────────────────────────── */}
      {showPasswordModal && (
        <div className="fixed inset-0 bg-slate-950/60 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-md w-full p-6 animate-in zoom-in-95">
            <div className="flex items-center justify-between pb-4 border-b border-slate-100">
              <div className="flex items-center gap-2">
                <KeyRound className="w-5 h-5 text-blue-600" />
                <h3 className="text-base font-bold text-slate-900">Change Account Password</h3>
              </div>
              <button
                onClick={() => {
                  setShowPasswordModal(false);
                  setPasswordError(null);
                  setPasswordSuccess(null);
                }}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {passwordError && (
              <div className="mt-4 p-3 rounded-xl bg-red-50 border border-red-200 text-red-600 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{passwordError}</span>
              </div>
            )}

            {passwordSuccess && (
              <div className="mt-4 p-3 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-xs flex items-center gap-2">
                <CheckCircle2 className="w-4 h-4 shrink-0" />
                <span>{passwordSuccess}</span>
              </div>
            )}

            <form onSubmit={handleChangePasswordSubmit} className="my-5 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Current Password *
                </label>
                <input
                  type="password"
                  required
                  value={currentPass}
                  onChange={e => setCurrentPass(e.target.value)}
                  placeholder="Enter your current password"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  New Password *
                </label>
                <input
                  type="password"
                  required
                  value={newPass}
                  onChange={e => setNewPass(e.target.value)}
                  placeholder="Minimum 6 characters"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">
                  Confirm New Password *
                </label>
                <input
                  type="password"
                  required
                  value={confirmPass}
                  onChange={e => setConfirmPass(e.target.value)}
                  placeholder="Re-enter your new password"
                  className="w-full px-3 py-2 rounded-xl border border-slate-200 text-xs text-slate-900 focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => {
                    setShowPasswordModal(false);
                    setPasswordError(null);
                    setPasswordSuccess(null);
                  }}
                  className="px-4 py-2 rounded-xl bg-slate-100 text-slate-600 text-xs font-semibold hover:bg-slate-200"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold shadow-xs"
                >
                  Update Password
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
