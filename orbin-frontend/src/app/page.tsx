'use client';

// ============================================================================
// ORBIN SCHOOL - ROOT ENTRYPOINT ROUTER
// ============================================================================

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/context/auth-context';
import { Loader2 } from 'lucide-react';

export default function RootPage() {
  const router = useRouter();
  const { user, isLoading } = useAuth();

  useEffect(() => {
    if (!isLoading) {
      if (!user) {
        router.replace('/login');
      } else if (user.role === 'SUPER_ADMIN') {
        router.replace('/schools');
      } else if (user.role === 'TEACHER') {
        router.replace('/attendance');
      } else {
        router.replace('/dashboard');
      }
    }
  }, [user, isLoading, router]);

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center text-white">
      <div className="flex items-center gap-3 text-sm font-semibold text-slate-400">
        <Loader2 className="w-5 h-5 animate-spin text-blue-500" />
        <span>Initializing Orbin Multi-Tenant SaaS...</span>
      </div>
    </div>
  );
}
