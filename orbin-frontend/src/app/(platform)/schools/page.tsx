'use client';

// ============================================================================
// ORBIN SCHOOL - PLATFORM MULTI-TENANT SCHOOLS DIRECTORY (Super Admin)
// ============================================================================

import React, { useState } from 'react';
import {
  Building2,
  Plus,
  Check,
  X,
  KeyRound,
  CheckCircle2,
  AlertCircle
} from 'lucide-react';
import { useAuth } from '@/context/auth-context';

export default function TenantSchoolsPage() {
  const { schools, onboardSchoolWithAccount } = useAuth();
  const [showModal, setShowModal] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // New School Form Fields
  const [name, setName] = useState('');
  const [slug, setSlug] = useState('');
  const [board, setBoard] = useState('CBSE Affiliated');
  const [city, setCity] = useState('');
  const [color, setColor] = useState('#2563eb');
  const [email, setEmail] = useState('');
  const [initialPassword, setInitialPassword] = useState('School@2026');

  // Success Notice after Provisioning
  const [provisionedNotice, setProvisionedNotice] = useState<{
    schoolName: string;
    email: string;
    pass: string;
  } | null>(null);

  const handleOnboard = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    try {
      const res = await onboardSchoolWithAccount(
        {
          name,
          slug,
          board,
          city,
          branding: { primaryColor: color, secondaryColor: color, accentColor: '#38bdf8' }
        },
        email,
        initialPassword
      );

      setProvisionedNotice({
        schoolName: res.school.name,
        email: res.email,
        pass: initialPassword
      });

      setShowModal(false);
      setName('');
      setSlug('');
      setCity('');
      setEmail('');
      setInitialPassword('School@2026');
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to provision school';
      setError(msg);
    }
  };

  return (
    <div className="space-y-6">
      {/* Newly Provisioned Credentials Notice Strip */}
      {provisionedNotice && (
        <div className="p-5 rounded-2xl bg-purple-950/70 border border-purple-500/30 text-white shadow-xl animate-in fade-in flex flex-col sm:flex-row sm:items-center justify-between gap-4">
          <div className="flex items-start gap-3">
            <CheckCircle2 className="w-5 h-5 text-emerald-400 mt-0.5 shrink-0" />
            <div>
              <div className="text-sm font-bold text-white">
                Tenant &quot;{provisionedNotice.schoolName}&quot; Provisioned Successfully!
              </div>
              <div className="text-xs text-purple-200 mt-1">
                Share these initial credentials with the school administrator. They can change this password after signing in:
              </div>
              <div className="mt-2 flex items-center gap-4 text-xs font-mono bg-slate-950/60 p-2.5 rounded-xl border border-purple-500/20">
                <span>Email ID: <b className="text-white">{provisionedNotice.email}</b></span>
                <span className="text-purple-400">&bull;</span>
                <span>Initial Password: <b className="text-purple-300">{provisionedNotice.pass}</b></span>
              </div>
            </div>
          </div>
          <button
            type="button"
            onClick={() => setProvisionedNotice(null)}
            className="p-1.5 rounded-lg bg-slate-900 text-slate-400 hover:text-white shrink-0"
          >
            <X className="w-4 h-4" />
          </button>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-xl font-extrabold text-white tracking-tight flex items-center gap-2">
            <Building2 className="w-6 h-6 text-purple-400" />
            <span>Platform Tenant Schools Directory</span>
          </h1>
          <p className="text-xs text-slate-400 mt-1">
            Provision educational institutions with admin credentials and isolated database scopes
          </p>
        </div>

        <button
          type="button"
          onClick={() => setShowModal(true)}
          className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white font-semibold text-xs shadow-md shadow-purple-600/25 flex items-center gap-2 transition-all active:scale-[0.99]"
        >
          <Plus className="w-4 h-4" />
          <span>Provision Tenant School</span>
        </button>
      </div>

      {/* Tenant Cards Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {schools.map(s => (
          <div
            key={s.id}
            className="bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-xl flex flex-col justify-between"
            style={{ borderTop: `4px solid ${s.branding.primaryColor}` }}
          >
            <div>
              <div className="flex items-center justify-between mb-4">
                <div
                  className="w-10 h-10 rounded-xl flex items-center justify-center font-extrabold text-white text-xs shadow-md"
                  style={{ backgroundColor: s.branding.primaryColor }}
                >
                  {s.name.split(' ').map(w => w[0]).join('').slice(0, 3)}
                </div>
                <span className="font-mono text-xs px-2.5 py-1 rounded-md bg-slate-950 text-slate-400 border border-slate-800">
                  {s.slug}.orbin.edu
                </span>
              </div>

              <h3 className="text-base font-bold text-white leading-snug">{s.name}</h3>
              <p className="text-xs text-slate-400 mt-1">{s.board} &bull; {s.city}</p>

              {/* Module Toggles Matrix */}
              <div className="mt-5 pt-4 border-t border-slate-800">
                <div className="text-[10px] font-bold uppercase tracking-wider text-slate-400 mb-2.5">
                  SaaS Feature Modules Toggles
                </div>
                <div className="grid grid-cols-2 gap-2 text-xs">
                  {Object.entries(s.activeModules).map(([modKey, active]) => (
                    <div
                      key={modKey}
                      className={`p-2 rounded-lg border text-left flex items-center justify-between text-[11px] font-medium transition-all ${
                        active
                          ? 'bg-purple-500/10 border-purple-500/30 text-purple-300'
                          : 'bg-slate-950 border-slate-800 text-slate-500'
                      }`}
                    >
                      <span className="capitalize">{modKey}</span>
                      {active ? <Check className="w-3.5 h-3.5 text-purple-400" /> : <X className="w-3.5 h-3.5 text-slate-600" />}
                    </div>
                  ))}
                </div>
              </div>
            </div>

            <div className="mt-6 pt-4 border-t border-slate-800/80 flex items-center justify-between text-xs text-slate-400">
              <span className="truncate">{s.email}</span>
              <span className="text-emerald-400 font-bold bg-emerald-500/10 px-2 py-0.5 rounded text-[10px]">
                Active
              </span>
            </div>
          </div>
        ))}
      </div>

      {/* Onboard Tenant Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-slate-950/80 backdrop-blur-xs z-50 flex items-center justify-center p-4">
          <div className="bg-slate-900 border border-slate-800 text-white rounded-2xl shadow-2xl max-w-lg w-full p-6 animate-in zoom-in-95">
            <div className="flex items-center justify-between pb-4 border-b border-slate-800">
              <h3 className="text-base font-bold text-white">Provision New Tenant School</h3>
              <button onClick={() => setShowModal(false)} className="p-1 text-slate-400 hover:text-white">
                <X className="w-5 h-5" />
              </button>
            </div>

            {error && (
              <div className="mt-4 p-3 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs flex items-center gap-2">
                <AlertCircle className="w-4 h-4 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <form onSubmit={handleOnboard} className="my-5 space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">School Full Name *</label>
                <input
                  type="text"
                  required
                  value={name}
                  onChange={e => setName(e.target.value)}
                  placeholder="e.g. Heritage Public School"
                  className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Subdomain Slug *</label>
                  <input
                    type="text"
                    required
                    value={slug}
                    onChange={e => setSlug(e.target.value)}
                    placeholder="heritage"
                    className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Affiliation Board</label>
                  <input
                    type="text"
                    value={board}
                    onChange={e => setBoard(e.target.value)}
                    placeholder="CBSE / ICSE / IB"
                    className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">City</label>
                  <input
                    type="text"
                    value={city}
                    onChange={e => setCity(e.target.value)}
                    placeholder="New Delhi"
                    className="w-full px-3 py-2 rounded-xl bg-slate-950 border border-slate-800 text-xs text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Brand Theme Color</label>
                  <input
                    type="color"
                    value={color}
                    onChange={e => setColor(e.target.value)}
                    className="w-full h-8 px-1 py-1 rounded-xl bg-slate-950 border border-slate-800 focus:outline-none cursor-pointer"
                  />
                </div>
              </div>

              {/* Required Email & Initial Password for School Admin */}
              <div className="p-4 rounded-xl bg-slate-950/80 border border-purple-500/20 space-y-3">
                <div className="text-xs font-bold text-purple-300 flex items-center gap-2">
                  <KeyRound className="w-4 h-4" />
                  <span>Initial School Admin Credentials</span>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">School Admin Email ID *</label>
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={e => setEmail(e.target.value)}
                    placeholder="admin@heritage.edu"
                    className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs text-white focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1">Initial Password *</label>
                  <input
                    type="text"
                    required
                    value={initialPassword}
                    onChange={e => setInitialPassword(e.target.value)}
                    placeholder="Initial password for school"
                    className="w-full px-3 py-2 rounded-xl bg-slate-900 border border-slate-700 text-xs font-mono text-purple-200 focus:outline-none focus:ring-2 focus:ring-purple-500"
                  />
                  <p className="text-[11px] text-slate-400 mt-1">
                    The school admin can change this password after signing in.
                  </p>
                </div>
              </div>

              <div className="flex justify-end gap-2 pt-3">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-xl bg-slate-800 text-slate-300 text-xs font-semibold hover:bg-slate-700"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold shadow-xs"
                >
                  Provision School & Credentials
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
