'use client';

// ============================================================================
// ORBIN SCHOOL - PLATFORM SUPER ADMIN LAYOUT (SaaS Owner Console)
// ============================================================================

import React from 'react';
import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useAuth } from '@/context/auth-context';
import {
  Shield,
  Building2,
  LogOut
} from 'lucide-react';

export default function PlatformLayout({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { logout } = useAuth();

  const handleLogout = () => {
    logout();
    router.push('/login');
  };

  return (
    <div className="flex h-screen bg-slate-950 text-slate-100 overflow-hidden">
      {/* ── Super Admin Dark Sidebar ─────────────────────────────────────── */}
      <aside className="w-64 bg-slate-900 border-r border-slate-800/80 flex flex-col shrink-0">
        {/* Brand Header */}
        <div className="h-16 px-5 border-b border-slate-800 flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-purple-600 to-indigo-500 text-white flex items-center justify-center font-bold shadow-md shadow-purple-500/20">
            <Shield className="w-5 h-5" />
          </div>
          <div>
            <div className="text-sm font-bold text-white tracking-tight">
              Orbin SaaS Admin
            </div>
            <div className="text-[10px] text-purple-400 font-semibold uppercase tracking-wider">
              Platform Master Console
            </div>
          </div>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-1">
          <div className="px-3 pb-2 text-[10px] font-bold text-slate-500 uppercase tracking-wider">
            Tenants & Platform
          </div>

          <Link
            href="/schools"
            className={`flex items-center gap-3 px-3.5 py-2.5 rounded-xl text-xs font-semibold transition-all ${pathname.startsWith('/schools')
                ? 'bg-purple-600 text-white shadow-md shadow-purple-600/25'
                : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
          >
            <Building2 className="w-4 h-4" />
            <span>Tenant Schools Directory</span>
          </Link>
        </nav>

        {/* Sign Out Button */}
        <div className="p-3 border-t border-slate-800">
          <button
            type="button"
            onClick={handleLogout}
            className="w-full py-2.5 px-3 rounded-xl bg-slate-800/60 hover:bg-slate-800 text-slate-300 text-xs font-semibold flex items-center justify-center gap-2 border border-slate-700/60 transition-all"
          >
            <LogOut className="w-3.5 h-3.5" />
            <span>Sign Out</span>
          </button>
        </div>
      </aside>

      {/* ── Main View Area ───────────────────────────────────────────────── */}
      <div className="flex-1 flex flex-col min-w-0 overflow-hidden bg-slate-950">
        {/* Topbar */}
        <header className="h-16 border-b border-slate-800/80 px-8 flex items-center justify-between shrink-0 bg-slate-900/50 backdrop-blur-md">
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold px-2.5 py-1 rounded-md bg-purple-500/10 text-purple-400 border border-purple-500/20">
              admin.orbin.edu
            </span>
            <span className="text-xs text-slate-500">&bull;</span>
            <span className="text-xs text-slate-400 font-medium">
              Multi-Tenant Cluster Isolation: Active
            </span>
          </div>

          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-purple-600 text-white font-bold text-xs flex items-center justify-center">
                P
              </div>
              <div className="text-left text-xs">
                <div className="font-bold text-white">Partha</div>
                <div className="text-purple-400 text-[10px]">Platform Owner</div>
              </div>
            </div>

            <button
              onClick={handleLogout}
              className="p-2 rounded-lg text-slate-400 hover:text-red-400 hover:bg-red-500/10 transition-all"
              title="Sign Out"
            >
              <LogOut className="w-4 h-4" />
            </button>
          </div>
        </header>

        {/* Content */}
        <main className="flex-1 overflow-y-auto p-8">
          <div className="max-w-7xl mx-auto">
            {children}
          </div>
        </main>
      </div>
    </div>
  );
}
