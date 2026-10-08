'use client';

// ============================================================================
// ORBIN SCHOOL - UNIFIED AUTHENTICATION & LOGIN PAGE
// ============================================================================

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/auth-context';
import { School, ArrowRight, Loader2, AlertCircle, Shield, GraduationCap } from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();

  const [email, setEmail] = useState('admin@delhipublicacademy.edu');
  const [password, setPassword] = useState('school123');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const result = await login(email, password);
      if (result.role === 'SUPER_ADMIN') {
        router.push('/schools');
      } else if (result.role === 'TEACHER') {
        router.push('/attendance');
      } else {
        router.push('/dashboard');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Invalid email or password.';
      setError(msg);
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (em: string, pw: string) => {
    setEmail(em);
    setPassword(pw);
    setError(null);
  };

  return (
    <div className="min-h-screen bg-slate-950 flex flex-col justify-center items-center p-6 relative overflow-hidden">
      {/* Background Decorative Glow */}
      <div className="absolute top-1/4 left-1/2 -translate-x-1/2 w-[600px] h-[350px] bg-blue-600/15 rounded-full blur-[120px] pointer-events-none" />

      <div className="w-full max-w-md relative z-10">
        {/* Brand Header */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-2xl bg-gradient-to-tr from-blue-600 to-cyan-500 shadow-lg shadow-blue-500/25 mb-4 text-white">
            <School className="w-7 h-7" />
          </div>
          <h1 className="text-2xl font-extrabold text-white tracking-tight">
            Orbin School SaaS
          </h1>
          <p className="text-sm text-slate-400 mt-1">
            Single Unified Sign-In for Platform Admin & Tenant Schools
          </p>
        </div>

        {/* Login Card */}
        <div className="bg-slate-900 border border-slate-800 rounded-2xl p-8 shadow-2xl backdrop-blur-xl">
          {error && (
            <div className="mb-6 p-4 rounded-xl bg-red-500/10 border border-red-500/20 text-red-400 text-xs flex items-start gap-3">
              <AlertCircle className="w-4 h-4 mt-0.5 shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Registered Email ID
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-800 text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                placeholder="name@school.edu"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-2">
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-xl bg-slate-950 border border-slate-800 text-white placeholder-slate-500 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                placeholder="••••••••••••"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-3.5 px-4 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-sm shadow-lg shadow-blue-600/30 flex items-center justify-center gap-2 transition-all active:scale-[0.99] disabled:opacity-50"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  <span>Verifying Credentials...</span>
                </>
              ) : (
                <>
                  <span>Sign In</span>
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>
          </form>

          {/* Quick Credential Fillers for Testing */}
          <div className="mt-8 pt-6 border-t border-slate-800/80">
            <div className="text-[11px] font-bold text-slate-400 uppercase tracking-wider mb-2.5 text-center">
              Quick Test Credentials
            </div>
            <div className="space-y-2 text-xs">
              <div
                onClick={() => fillCredentials('parthasarathye2256@gmail.com', 'Partha@2256')}
                className="p-2.5 rounded-xl bg-slate-950/80 hover:bg-slate-800 border border-slate-800 flex items-center justify-between cursor-pointer transition-colors"
              >
                <div>
                  <div className="font-bold text-purple-400 flex items-center gap-1.5">
                    <Shield className="w-3.5 h-3.5" />
                    <span>Platform Super Admin (Partha)</span>
                  </div>
                  <div className="text-slate-400 font-mono text-[11px]">parthasarathye2256@gmail.com / Partha@2256</div>
                </div>
                <span className="text-[10px] text-slate-500 font-semibold bg-slate-900 px-2 py-1 rounded">Fill</span>
              </div>

              <div
                onClick={() => fillCredentials('admin@delhipublicacademy.edu', 'school123')}
                className="p-2.5 rounded-xl bg-slate-950/80 hover:bg-slate-800 border border-slate-800 flex items-center justify-between cursor-pointer transition-colors"
              >
                <div>
                  <div className="font-bold text-blue-400 flex items-center gap-1.5">
                    <School className="w-3.5 h-3.5" />
                    <span>School Admin (Delhi Public)</span>
                  </div>
                  <div className="text-slate-400 font-mono text-[11px]">admin@delhipublicacademy.edu / school123</div>
                </div>
                <span className="text-[10px] text-slate-500 font-semibold bg-slate-900 px-2 py-1 rounded">Fill</span>
              </div>

              <div
                onClick={() => fillCredentials('teacher@delhipublicacademy.edu', 'teacher123')}
                className="p-2.5 rounded-xl bg-slate-950/80 hover:bg-slate-800 border border-slate-800 flex items-center justify-between cursor-pointer transition-colors"
              >
                <div>
                  <div className="font-bold text-emerald-400 flex items-center gap-1.5">
                    <GraduationCap className="w-3.5 h-3.5" />
                    <span>Teacher / Faculty (Delhi Public)</span>
                  </div>
                  <div className="text-slate-400 font-mono text-[11px]">teacher@delhipublicacademy.edu / teacher123</div>
                </div>
                <span className="text-[10px] text-slate-500 font-semibold bg-slate-900 px-2 py-1 rounded">Fill</span>
              </div>
            </div>
          </div>
        </div>

        {/* Security Footer */}
        <div className="text-center mt-6 text-xs text-slate-500">
          Strict Multi-Tenant Isolation &bull; Self-Service Password Reset
        </div>
      </div>
    </div>
  );
}
