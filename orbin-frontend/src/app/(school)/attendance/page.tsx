'use client';

// ============================================================================
// ORBIN SCHOOL - 1-CLICK ATTENDANCE & AUTOMATED WHATSAPP ABSENCE ALERTS
// Connected directly to Spring Boot REST APIs (/api/v1/attendance, /api/v1/whatsapp)
// ============================================================================

import React, { useState, useEffect, useCallback } from 'react';
import {
  CheckCircle2,
  Save,
  CheckCheck,
  Send,
  MessageSquare,
  X,
  Clock,
  Check,
  Share2,
  Loader2,
  AlertCircle,
  RefreshCw,
  Users
} from 'lucide-react';
import { AttendanceStatus, WhatsAppNotificationDto, StudentResponse, SchoolClass, Section } from '@/lib/types';
import { api } from '@/lib/api';
import { useAuth } from '@/context/auth-context';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';

interface StudentAttendanceRow {
  id: string;
  rollNo: number;
  name: string;
  admissionNo: string;
  parentName: string;
  parentPhone: string;
  status: AttendanceStatus;
}

export default function AttendancePage() {
  const { currentSchool } = useAuth();
  const searchParams = useSearchParams();
  const querySectionId = searchParams?.get('sectionId');

  const [students, setStudents] = useState<StudentAttendanceRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [date, setDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [section, setSection] = useState(querySectionId || '1');
  const [sectionsList, setSectionsList] = useState<Section[]>([]);
  const [saving, setSaving] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Load real sections from PostgreSQL via Spring Boot
  useEffect(() => {
    async function loadAcademicSections() {
      try {
        const clsList = await api.getClasses();
        if (clsList && clsList.length > 0) {
          const allSecs: Section[] = [];
          for (const c of clsList) {
            try {
              const secs = await api.getSections(String(c.id));
              if (secs) allSecs.push(...secs);
            } catch {}
          }
          setSectionsList(allSecs);
          if (querySectionId) {
            setSection(querySectionId);
          } else if (allSecs.length > 0 && section === '1') {
            setSection(String(allSecs[0].id));
          }
        }
      } catch (err) {
        console.error('Failed to load classes/sections in attendance', err);
      }
    }
    loadAcademicSections();
  }, [querySectionId]);

  // WhatsApp Broadcast Modal State
  const [showWhatsAppModal, setShowWhatsAppModal] = useState(false);
  const [isDispatching, setIsDispatching] = useState(false);
  const [dispatchComplete, setDispatchComplete] = useState(false);

  // WhatsApp Audit Log Drawer State
  const [showLogDrawer, setShowLogDrawer] = useState(false);
  const [notificationLogs, setNotificationLogs] = useState<WhatsAppNotificationDto[]>([]);
  const [loadingLogs, setLoadingLogs] = useState(false);

  // Load WhatsApp Logs from backend
  const fetchWhatsAppLogs = useCallback(async () => {
    try {
      setLoadingLogs(true);
      const logs = await api.getWhatsAppLogs();
      setNotificationLogs(logs || []);
    } catch {
      // Backend log retrieval gracefully handled
    } finally {
      setLoadingLogs(false);
    }
  }, []);

  // Fetch live student roster and any existing attendance for this date & section
  const fetchRosterAndAttendance = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Fetch real students from PostgreSQL
      const studentData: StudentResponse[] = await api.getStudents({
        sectionId: section !== 'all' ? section : undefined,
      });

      // 2. Fetch any already-recorded attendance for today
      const existingAttendanceMap: Record<string, AttendanceStatus> = {};
      try {
        const attendanceRecords = await api.getAttendance(section, date);
        if (Array.isArray(attendanceRecords)) {
          attendanceRecords.forEach((rec: any) => {
            const sid = String(rec.studentId);
            existingAttendanceMap[sid] = (rec.status as AttendanceStatus) || 'PRESENT';
          });
        }
      } catch {
        // Attendance not marked yet today
      }

      const rows: StudentAttendanceRow[] = (studentData || []).map((s, idx) => ({
        id: String(s.id),
        rollNo: s.rollNo || idx + 1,
        name: s.fullName || `${s.firstName || ''} ${s.lastName || ''}`.trim() || 'Student',
        admissionNo: s.admissionNo || `ADM-${s.id}`,
        parentName: s.parentName || 'Parent / Guardian',
        parentPhone: s.parentPhone || '+91 98765 00000',
        status: existingAttendanceMap[String(s.id)] || 'PRESENT',
      }));

      setStudents(rows);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to fetch student roll list';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, [section, date]);

  useEffect(() => {
    fetchRosterAndAttendance();
    fetchWhatsAppLogs();
  }, [fetchRosterAndAttendance, fetchWhatsAppLogs]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  const setStatus = (studentId: string, status: AttendanceStatus) => {
    setStudents(prev =>
      prev.map(s => (s.id === studentId ? { ...s, status } : s))
    );
  };

  const markAllPresent = () => {
    setStudents(prev => prev.map(s => ({ ...s, status: 'PRESENT' })));
    showToast('Marked all students as PRESENT');
  };

  const absentees = students.filter(s => s.status === 'ABSENT');
  const presentCount = students.filter(s => s.status === 'PRESENT').length;
  const absentCount = absentees.length;
  const lateCount = students.filter(s => s.status === 'LATE').length;

  // Save Attendance to backend database
  const handleSaveAttendance = async () => {
    if (students.length === 0) {
      showToast('No students loaded to mark attendance for.');
      return;
    }

    setSaving(true);
    try {
      await api.markAttendance({
        sectionId: section,
        date,
        records: students.map(s => ({ studentId: s.id, status: s.status }))
      });
      showToast('Attendance register successfully saved & synced to PostgreSQL!');

      if (absentCount > 0) {
        setDispatchComplete(false);
        setShowWhatsAppModal(true);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to record attendance';
      showToast(msg);
    } finally {
      setSaving(false);
    }
  };

  // Dispatch Automated WhatsApp Blast via Meta Cloud WhatsApp API
  const handleDispatchWhatsAppBlast = async () => {
    setIsDispatching(true);

    try {
      await api.sendAbsenceBlast({
        templateType: 'ABSENCE_ALERT',
        recipients: absentees.map(s => ({
          studentId: s.id,
          studentName: s.name,
          parentName: s.parentName,
          parentPhone: s.parentPhone,
          customMessage: `Dear ${s.parentName}, your child ${s.name} has been marked ABSENT on ${date} at ${currentSchool?.name || 'School'}.`
        }))
      });

      // Refresh log drawer with newly created notifications
      await fetchWhatsAppLogs();

      setIsDispatching(false);
      setDispatchComplete(true);
      showToast(`Dispatched ${absentees.length} WhatsApp absence alert(s) to parents via official Meta API!`);
    } catch (err: unknown) {
      setIsDispatching(false);
      const msg = err instanceof Error ? err.message : 'WhatsApp transmission error';
      showToast(msg);
    }
  };

  return (
    <div className="space-y-6">
      {/* ── Toast Alert ───────────────────────────────────────────────────── */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 p-4 rounded-xl bg-slate-900 text-white shadow-2xl border border-slate-700 flex items-center gap-3 text-xs font-semibold animate-in slide-in-from-bottom-2">
          <CheckCheck className="w-4 h-4 text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* ── Page Header ────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900">Daily Attendance Register</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-blue-50 text-blue-700 border border-blue-200">
              {currentSchool?.name || 'Class Attendance'}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            One-click student roll call with instant automated parent WhatsApp absence alerts.
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={fetchRosterAndAttendance}
            disabled={loading}
            className="p-2.5 bg-slate-50 hover:bg-slate-100 text-slate-700 rounded-xl text-xs font-semibold border border-slate-200 transition-all active:scale-95 disabled:opacity-50"
            title="Refresh student register"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>

          <button
            type="button"
            onClick={() => setShowLogDrawer(true)}
            className="px-3.5 py-2 rounded-xl bg-slate-50 hover:bg-slate-100 text-slate-700 border border-slate-200 font-semibold text-xs flex items-center gap-2 transition-colors"
          >
            <Clock className="w-4 h-4 text-slate-500" />
            <span>WhatsApp Log ({notificationLogs.length})</span>
          </button>

          <button
            type="button"
            onClick={markAllPresent}
            disabled={students.length === 0}
            className="px-3.5 py-2 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs border border-slate-200 shadow-xs flex items-center gap-2 transition-all disabled:opacity-50"
          >
            <CheckCircle2 className="w-4 h-4 text-emerald-600" />
            <span>Mark All Present</span>
          </button>

          <button
            type="button"
            disabled={saving || students.length === 0}
            onClick={handleSaveAttendance}
            className="px-4 py-2 rounded-xl bg-blue-600 hover:bg-blue-500 text-white font-semibold text-xs shadow-md shadow-blue-600/25 flex items-center gap-2 transition-all active:scale-99 disabled:opacity-50"
          >
            {saving ? <Loader2 className="w-4 h-4 animate-spin" /> : <Save className="w-4 h-4" />}
            <span>{saving ? 'Syncing to PostgreSQL...' : 'Save & Alert Parents'}</span>
          </button>
        </div>
      </div>

      {/* ── Server Error Banner ───────────────────────────────────────────── */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
            <span>{error}</span>
          </div>
          <button
            onClick={fetchRosterAndAttendance}
            className="px-3 py-1 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 text-xs"
          >
            Retry
          </button>
        </div>
      )}

      {/* ── KPI & Controls Toolbar ────────────────────────────────────────── */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        {/* Date & Section Selectors */}
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col justify-between">
          <span className="text-[11px] font-bold uppercase tracking-wider text-slate-400">Date & Section</span>
          <div className="grid grid-cols-2 gap-2 mt-2">
            <input
              type="date"
              value={date}
              onChange={e => setDate(e.target.value)}
              className="px-2 py-1.5 rounded-lg border border-slate-200 text-xs font-semibold text-slate-800"
            />
            <select
              value={section}
              onChange={e => setSection(e.target.value)}
              className="px-2 py-1.5 rounded-lg border border-slate-200 text-xs font-semibold text-slate-800 bg-white"
            >
              {sectionsList.length === 0 ? (
                <option value="1">Section A</option>
              ) : (
                sectionsList.map((sec) => (
                  <option key={sec.id} value={sec.id}>
                    {sec.className ? `${sec.className} - ` : ''}Section {sec.name}
                  </option>
                ))
              )}
              <option value="all">All Sections</option>
            </select>
          </div>
        </div>

        {/* Present KPI */}
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <span className="text-[11px] font-bold uppercase tracking-wider text-emerald-600">Present</span>
          <div className="text-2xl font-black text-slate-900 mt-1">{loading ? '—' : presentCount}</div>
          <div className="text-[11px] text-slate-400 mt-0.5">
            {students.length > 0 ? `${Math.round((presentCount / students.length) * 100)}% present today` : 'No records'}
          </div>
        </div>

        {/* Absent KPI */}
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <span className="text-[11px] font-bold uppercase tracking-wider text-rose-600">Absent</span>
          <div className="text-2xl font-black text-rose-600 mt-1">{loading ? '—' : absentCount}</div>
          <div className="text-[11px] text-rose-700 font-medium mt-0.5">
            {absentCount > 0 ? 'Requires WhatsApp Alert' : 'Zero absences'}
          </div>
        </div>

        {/* Late KPI */}
        <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs">
          <span className="text-[11px] font-bold uppercase tracking-wider text-amber-600">Late Arrival</span>
          <div className="text-2xl font-black text-amber-600 mt-1">{loading ? '—' : lateCount}</div>
          <div className="text-[11px] text-slate-400 mt-0.5">Marked delayed entry</div>
        </div>
      </div>

      {/* ── Attendance Roll Call Table ────────────────────────────────────── */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {loading ? (
          <div className="py-20 flex flex-col items-center justify-center text-slate-400 gap-3">
            <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
            <p className="text-xs font-semibold">Connecting to PostgreSQL and loading student roll call register...</p>
          </div>
        ) : students.length === 0 ? (
          <div className="py-16 px-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
              <Users className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-slate-800">No Students Found in This Section</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1 mb-4">
              There are currently no students enrolled for this section in the database.
            </p>
            <Link
              href="/students"
              className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-xs font-semibold rounded-xl inline-flex items-center gap-1.5 shadow-sm"
            >
              <span>Enroll Students Now</span>
            </Link>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4 w-12 text-center">Roll</th>
                  <th className="py-3.5 px-4">Student Name</th>
                  <th className="py-3.5 px-4">Admission No</th>
                  <th className="py-3.5 px-4">Parent / Contact</th>
                  <th className="py-3.5 px-4 text-center">Status Toggle</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {students.map(s => (
                  <tr key={s.id} className="hover:bg-slate-50/60 transition-colors">
                    <td className="py-3.5 px-4 text-center font-bold text-slate-400">{s.rollNo}</td>
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-slate-900">{s.name}</div>
                    </td>
                    <td className="py-3.5 px-4 font-mono text-[11px] text-slate-500">{s.admissionNo}</td>
                    <td className="py-3.5 px-4">
                      <div className="text-slate-800">{s.parentName}</div>
                      <div className="font-mono text-[11px] text-slate-400">{s.parentPhone}</div>
                    </td>
                    <td className="py-3.5 px-4 text-center">
                      <div className="inline-flex rounded-xl p-1 bg-slate-100 border border-slate-200 gap-1">
                        <button
                          type="button"
                          onClick={() => setStatus(s.id, 'PRESENT')}
                          className={`px-3 py-1 rounded-lg font-bold text-[11px] transition-all ${
                            s.status === 'PRESENT'
                              ? 'bg-emerald-600 text-white shadow-xs'
                              : 'text-slate-600 hover:text-slate-900'
                          }`}
                        >
                          Present
                        </button>
                        <button
                          type="button"
                          onClick={() => setStatus(s.id, 'ABSENT')}
                          className={`px-3 py-1 rounded-lg font-bold text-[11px] transition-all ${
                            s.status === 'ABSENT'
                              ? 'bg-rose-600 text-white shadow-xs'
                              : 'text-slate-600 hover:text-slate-900'
                          }`}
                        >
                          Absent
                        </button>
                        <button
                          type="button"
                          onClick={() => setStatus(s.id, 'LATE')}
                          className={`px-3 py-1 rounded-lg font-bold text-[11px] transition-all ${
                            s.status === 'LATE'
                              ? 'bg-amber-500 text-white shadow-xs'
                              : 'text-slate-600 hover:text-slate-900'
                          }`}
                        >
                          Late
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Modal: Automated Parent WhatsApp Absence Alert Blast ──────────── */}
      {showWhatsAppModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-2xl w-full max-w-lg p-6 shadow-2xl relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setShowWhatsAppModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-2 mb-1">
              <div className="p-2 rounded-xl bg-emerald-50 text-emerald-600">
                <MessageSquare className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-bold text-slate-900">Automated Parent WhatsApp Alert</h2>
                <div className="text-[11px] text-slate-500">
                  {absentCount} student{absentCount > 1 ? 's' : ''} marked absent on {date}
                </div>
              </div>
            </div>

            <p className="text-xs text-slate-600 my-4">
              Meta Cloud WhatsApp API ready to dispatch official absence notifications to registered parent numbers:
            </p>

            {/* Absentee Parent List */}
            <div className="space-y-2 max-h-56 overflow-y-auto border border-slate-200 rounded-xl p-2 bg-slate-50">
              {absentees.map(s => (
                <div key={s.id} className="p-3 bg-white rounded-lg border border-slate-200/80 text-xs space-y-1">
                  <div className="flex justify-between items-center">
                    <span className="font-bold text-slate-900">{s.name} ({s.admissionNo})</span>
                    <span className="font-mono text-[11px] text-emerald-700 font-semibold">{s.parentPhone}</span>
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Parent: <b>{s.parentName}</b>
                  </div>
                  <div className="pt-1 flex justify-between items-center text-[10px] text-slate-400 border-t border-slate-100">
                    <span>Template: school_absence_v1</span>
                    <button
                      type="button"
                      onClick={() => {
                        const msg = encodeURIComponent(
                          `Dear ${s.parentName}, your child ${s.name} has been marked ABSENT on ${date} at ${currentSchool?.name || 'School'}. If this is unexpected, please contact the school office.`
                        );
                        window.open(`https://wa.me/?text=${msg}`, '_blank');
                      }}
                      className="text-emerald-600 font-bold hover:underline flex items-center gap-1"
                    >
                      <Share2 className="w-3 h-3" />
                      <span>Direct Chat</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Actions */}
            <div className="mt-5 flex justify-end gap-2.5 pt-4 border-t border-slate-100 text-xs">
              <button
                type="button"
                onClick={() => setShowWhatsAppModal(false)}
                className="px-4 py-2 border border-slate-200 rounded-xl text-slate-600 font-semibold hover:bg-slate-50"
              >
                Close
              </button>

              <button
                type="button"
                disabled={isDispatching || dispatchComplete}
                onClick={handleDispatchWhatsAppBlast}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white rounded-xl font-semibold shadow-md shadow-emerald-600/25 flex items-center gap-2"
              >
                {dispatchComplete ? (
                  <>
                    <Check className="w-4 h-4 text-emerald-200" />
                    <span>Dispatched Successfully!</span>
                  </>
                ) : (
                  <>
                    {isDispatching ? <Loader2 className="w-4 h-4 animate-spin" /> : <Send className="w-4 h-4" />}
                    <span>{isDispatching ? 'Transmitting to WhatsApp API...' : `Dispatch to All ${absentCount} Parents`}</span>
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Drawer: WhatsApp Notification Audit Log ───────────────────────── */}
      {showLogDrawer && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-end p-0">
          <div className="bg-white border-l border-slate-200 w-full max-w-md h-full p-6 shadow-2xl flex flex-col justify-between animate-in slide-in-from-right">
            <div>
              <div className="flex justify-between items-center pb-4 border-b border-slate-200">
                <div className="flex items-center gap-2">
                  <MessageSquare className="w-5 h-5 text-emerald-600" />
                  <h2 className="text-base font-bold text-slate-900">WhatsApp Dispatch History</h2>
                </div>
                <button onClick={() => setShowLogDrawer(false)} className="text-slate-400 hover:text-slate-600">
                  <X className="w-5 h-5" />
                </button>
              </div>

              <div className="mt-4 space-y-3 overflow-y-auto max-h-[75vh]">
                {loadingLogs ? (
                  <div className="py-12 flex flex-col items-center justify-center text-slate-400 text-xs gap-2">
                    <Loader2 className="w-6 h-6 animate-spin text-emerald-600" />
                    <span>Fetching live audit log from PostgreSQL...</span>
                  </div>
                ) : notificationLogs.length === 0 ? (
                  <div className="py-12 text-center text-slate-400 text-xs">
                    No WhatsApp notifications logged yet for this school.
                  </div>
                ) : (
                  notificationLogs.map(log => (
                    <div key={log.id} className="p-3.5 rounded-xl border border-slate-200 bg-slate-50/70 text-xs space-y-1.5">
                      <div className="flex justify-between items-center">
                        <span className="font-bold text-slate-900">{log.studentName || 'Student'}</span>
                        <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-emerald-100 text-emerald-700">
                          {log.status}
                        </span>
                      </div>
                      <div className="text-[11px] text-slate-600">
                        Parent: <b>{log.recipientName}</b> &bull; <span className="font-mono">{log.recipientPhone}</span>
                      </div>
                      <div className="p-2 bg-white rounded-lg border border-slate-200/70 text-[11px] text-slate-600 italic">
                        &quot;{log.messageContent}&quot;
                      </div>
                      <div className="flex justify-between items-center text-[10px] text-slate-400 font-mono">
                        <span>{log.sentAt ? new Date(log.sentAt).toLocaleTimeString() : 'Just now'}</span>
                        <span>{log.metaMessageId ? `${log.metaMessageId.slice(0, 16)}...` : 'Meta Cloud API'}</span>
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>

            <div className="pt-4 border-t border-slate-200">
              <button
                type="button"
                onClick={() => setShowLogDrawer(false)}
                className="w-full py-2.5 bg-slate-900 text-white rounded-xl text-xs font-semibold hover:bg-slate-800 transition-colors"
              >
                Close Drawer
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
