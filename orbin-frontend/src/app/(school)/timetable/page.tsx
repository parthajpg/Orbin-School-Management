'use client';

// ============================================================================
// ORBIN SCHOOL - TIMETABLE & ACADEMIC COCKPIT (HM / TEACHER PORTAL)
// Connected directly to Spring Boot REST APIs (/api/v1/academic/timetable)
// Real PostgreSQL Persistence - Zero Mock Data
// ============================================================================

import React, { useState, useEffect, useCallback, useMemo } from 'react';
import Link from 'next/link';
import {
  CalendarDays,
  Clock,
  Upload,
  UserCheck,
  Building2,
  BookOpen,
  Sparkles,
  AlertCircle,
  CheckCircle2,
  Plus,
  Trash2,
  UserPlus,
  RefreshCw,
  FileSpreadsheet,
  Download,
  Check,
  X,
  Loader2,
  ArrowRight,
  ShieldAlert
} from 'lucide-react';
import {
  SchoolClass,
  Section,
  Subject,
  StaffDto,
  PeriodSlotDto,
  TimetableEntryDto,
  CreateTimetableEntryRequest,
  TimetableImportRow,
  TimetableValidationResult
} from '@/lib/types';
import { useAuth } from '@/context/auth-context';
import { api } from '@/lib/api';

const DAYS_OF_WEEK = [
  { id: 1, name: 'Monday' },
  { id: 2, name: 'Tuesday' },
  { id: 3, name: 'Wednesday' },
  { id: 4, name: 'Thursday' },
  { id: 5, name: 'Friday' },
  { id: 6, name: 'Saturday' },
];

export default function TimetablePage() {
  const { user, currentSchool } = useAuth();

  // Navigation Tabs
  const [activeTab, setActiveTab] = useState<'GRID' | 'LIVE' | 'UPLOAD' | 'MY_SCHEDULE'>('GRID');

  // Core Data
  const [classes, setClasses] = useState<SchoolClass[]>([]);
  const [sections, setSections] = useState<Section[]>([]);
  const [periodSlots, setPeriodSlots] = useState<PeriodSlotDto[]>([]);
  const [staffList, setStaffList] = useState<StaffDto[]>([]);
  const [classSubjects, setClassSubjects] = useState<Subject[]>([]);

  // Selection
  const [selectedClassId, setSelectedClassId] = useState<string>('');
  const [selectedSectionId, setSelectedSectionId] = useState<string>('');
  const [timetableEntries, setTimetableEntries] = useState<TimetableEntryDto[]>([]);
  const [mySchedule, setMySchedule] = useState<TimetableEntryDto[]>([]);

  // Loading & Error States
  const [loading, setLoading] = useState<boolean>(true);
  const [loadingGrid, setLoadingGrid] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Modal: Add Entry
  const [showAddModal, setShowAddModal] = useState<boolean>(false);
  const [modalDay, setModalDay] = useState<number>(1);
  const [modalSlotId, setModalSlotId] = useState<number | null>(null);
  const [modalTeacherId, setModalTeacherId] = useState<string>('');
  const [modalSubjectId, setModalSubjectId] = useState<string>('');
  const [modalRoom, setModalRoom] = useState<string>('Room 101');
  const [savingEntry, setSavingEntry] = useState<boolean>(false);

  // Modal: Proxy / Substitute
  const [showSubModal, setShowSubModal] = useState<boolean>(false);
  const [targetEntryForSub, setTargetEntryForSub] = useState<TimetableEntryDto | null>(null);
  const [selectedSubTeacherId, setSelectedSubTeacherId] = useState<string>('');
  const [savingSub, setSavingSub] = useState<boolean>(false);

  // CSV Ingestion State
  const [csvText, setCsvText] = useState<string>('');
  const [validationResult, setValidationResult] = useState<TimetableValidationResult | null>(null);
  const [validatingCsv, setValidatingCsv] = useState<boolean>(false);
  const [committingCsv, setCommittingCsv] = useState<boolean>(false);

  // Live Clock & Current Active Bell
  const [currentTime, setCurrentTime] = useState<Date>(new Date());

  useEffect(() => {
    const timer = setInterval(() => setCurrentTime(new Date()), 10000);
    return () => clearInterval(timer);
  }, []);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // 1. Initial Load: Classes, Slots, Staff
  const loadInitialData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [clsList, slots, staff] = await Promise.all([
        api.getClasses(),
        api.getPeriodSlots(),
        api.getStaffList(),
      ]);

      setClasses(clsList || []);
      setPeriodSlots(slots || []);
      setStaffList(staff || []);

      if (clsList && clsList.length > 0) {
        const firstClassId = String(clsList[0].id);
        setSelectedClassId(firstClassId);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load timetable base data';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInitialData();
  }, [loadInitialData]);

  // 2. When Class Changes, load Sections & Subjects
  useEffect(() => {
    if (!selectedClassId) return;

    async function loadSectionsAndSubjects() {
      try {
        const [secs, subs] = await Promise.all([
          api.getSections(selectedClassId),
          api.getSubjects(selectedClassId),
        ]);
        setSections(secs || []);
        setClassSubjects(subs || []);
        if (secs && secs.length > 0) {
          setSelectedSectionId(String(secs[0].id));
        } else {
          setSelectedSectionId('');
        }
      } catch (err) {
        console.error('Failed to load class sections/subjects', err);
      }
    }

    loadSectionsAndSubjects();
  }, [selectedClassId]);

  // 3. When Section Changes, load Weekly Timetable Entries
  const fetchSectionTimetable = useCallback(async () => {
    if (!selectedSectionId) {
      setTimetableEntries([]);
      return;
    }
    setLoadingGrid(true);
    try {
      const entries = await api.getTimetableForSection(selectedSectionId);
      setTimetableEntries(entries || []);
    } catch (err) {
      console.error('Failed to load section timetable', err);
    } finally {
      setLoadingGrid(false);
    }
  }, [selectedSectionId]);

  useEffect(() => {
    fetchSectionTimetable();
  }, [fetchSectionTimetable]);

  // 4. Load Teacher's personal schedule today
  const fetchMySchedule = useCallback(async () => {
    try {
      const schedule = await api.getMyScheduleToday();
      setMySchedule(schedule || []);
    } catch (err) {
      console.error('Failed to load my schedule', err);
    }
  }, []);

  useEffect(() => {
    fetchMySchedule();
  }, [fetchMySchedule]);

  // Current day index (1 = Monday, 6 = Saturday, 7 = Sunday)
  const currentDayIndex = useMemo(() => {
    const day = currentTime.getDay();
    return day === 0 ? 7 : day;
  }, [currentTime]);

  // Find currently active slot based on clock
  const activeSlot = useMemo(() => {
    const timeStr = currentTime.toTimeString().split(' ')[0]; // HH:mm:ss
    return periodSlots.find((s) => s.startTime <= timeStr && timeStr <= s.endTime);
  }, [currentTime, periodSlots]);

  // Check if current selected class is Primary / Nursery
  const isPrimarySelected = useMemo(() => {
    const currentCls = classes.find((c) => String(c.id) === selectedClassId);
    const name = (currentCls?.name || '').toLowerCase();
    return (
      name.includes('nursery') ||
      name.includes('lkg') ||
      name.includes('ukg') ||
      name.includes('class 1') ||
      name.includes('class 2') ||
      name.includes('class 3') ||
      name.includes('class 4') ||
      name.includes('class 5')
    );
  }, [selectedClassId, classes]);

  // Open modal to schedule slot
  const handleOpenScheduleModal = (dayId: number, slotId: number) => {
    setModalDay(dayId);
    setModalSlotId(slotId);

    // Default teacher to homeroom teacher if primary
    const currentSec = sections.find((s) => String(s.id) === selectedSectionId);
    if (currentSec?.classTeacherId) {
      setModalTeacherId(String(currentSec.classTeacherId));
    } else if (staffList.length > 0) {
      setModalTeacherId(String(staffList[0].userId || staffList[0].id));
    }

    if (classSubjects.length > 0) {
      setModalSubjectId(String(classSubjects[0].id));
    } else {
      setModalSubjectId('');
    }

    setShowAddModal(true);
  };

  // Submit Schedule Entry
  const handleSaveEntry = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSectionId || !modalSlotId || !modalTeacherId) {
      showToast('Please select all required fields.');
      return;
    }

    setSavingEntry(true);
    try {
      const payload: CreateTimetableEntryRequest = {
        sectionId: Number(selectedSectionId),
        periodSlotId: modalSlotId,
        dayOfWeek: modalDay,
        teacherId: Number(modalTeacherId),
        subjectId: modalSubjectId ? Number(modalSubjectId) : undefined,
        roomNumber: modalRoom.trim() || undefined,
      };

      await api.createTimetableEntry(payload);
      showToast('Schedule slot updated successfully!');
      setShowAddModal(false);
      fetchSectionTimetable();
      fetchMySchedule();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to schedule slot';
      showToast(msg);
    } finally {
      setSavingEntry(false);
    }
  };

  // Open Proxy / Substitute Modal
  const handleOpenSubModal = (entry: TimetableEntryDto) => {
    setTargetEntryForSub(entry);
    setSelectedSubTeacherId('');
    setShowSubModal(true);
  };

  // Submit Proxy Substitute
  const handleSaveSubstitute = async () => {
    if (!targetEntryForSub || !selectedSubTeacherId) {
      showToast('Please select a substitute teacher.');
      return;
    }

    setSavingSub(true);
    try {
      await api.assignSubstitute(targetEntryForSub.id, Number(selectedSubTeacherId));
      showToast('Proxy teacher assigned successfully!');
      setShowSubModal(false);
      fetchSectionTimetable();
      fetchMySchedule();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to assign substitute';
      showToast(msg);
    } finally {
      setSavingSub(false);
    }
  };

  // Delete Timetable Entry
  const handleDeleteEntry = async (entryId: number) => {
    if (!confirm('Are you sure you want to remove this scheduled class?')) return;
    try {
      await api.deleteTimetableEntry(entryId);
      showToast('Entry removed.');
      fetchSectionTimetable();
      fetchMySchedule();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to delete entry';
      showToast(msg);
    }
  };

  // Download Sample CSV Template
  const handleDownloadTemplate = () => {
    const csvContent =
      'Class,Section,DayOfWeek,PeriodNumber,SubjectCode,TeacherEmail,Room\n' +
      'Class 5,A,Monday,1,,sunita.rao@school.org,Room 101\n' +
      'Class 5,A,Monday,2,,sunita.rao@school.org,Room 101\n' +
      'Class 6,A,Monday,1,MATH,teacher@delhipublicacademy.edu,Room 204\n' +
      'Class 6,A,Monday,2,SCI,teacher@delhipublicacademy.edu,Science Lab 1\n' +
      'Class 6,B,Monday,1,ENG,teacher@delhipublicacademy.edu,Room 205\n';

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', 'orbin_timetable_template.csv');
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  // Parse CSV Rows Client-Side for Dry-Run Ingestion
  const parseCsvRows = (text: string): TimetableImportRow[] => {
    const lines = text
      .split('\n')
      .map((l) => l.trim())
      .filter((l) => l.length > 0);
    if (lines.length <= 1) return [];

    const rows: TimetableImportRow[] = [];
    // Skip header line
    for (let i = 1; i < lines.length; i++) {
      const parts = lines[i].split(',').map((p) => p.trim());
      if (parts.length >= 6) {
        rows.push({
          className: parts[0],
          sectionName: parts[1],
          day: parts[2],
          periodNumber: Number(parts[3]) || 1,
          subjectCode: parts[4] || undefined,
          teacherEmail: parts[5],
          roomNumber: parts[6] || undefined,
        });
      }
    }
    return rows;
  };

  // Handle CSV Dry-Run Validation
  const handleValidateCsv = async () => {
    const rows = parseCsvRows(csvText);
    if (rows.length === 0) {
      showToast('Please provide valid CSV rows matching the template.');
      return;
    }

    setValidatingCsv(true);
    setValidationResult(null);
    try {
      const result = await api.validateTimetableImport(rows);
      setValidationResult(result);
      if (result.canCommit) {
        showToast(`Pre-flight passed! ${result.validRows} entries ready to import.`);
      } else {
        showToast(`Pre-flight found ${result.errorRows} conflict(s). Please review below.`);
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Validation failed';
      showToast(msg);
    } finally {
      setValidatingCsv(false);
    }
  };

  // Handle CSV Commit Import
  const handleCommitCsv = async () => {
    const rows = parseCsvRows(csvText);
    if (rows.length === 0) return;

    setCommittingCsv(true);
    try {
      const result = await api.commitTimetableImport(rows);
      showToast(`Successfully imported ${result.validRows} timetable entries to PostgreSQL!`);
      setValidationResult(null);
      setCsvText('');
      setActiveTab('GRID');
      fetchSectionTimetable();
      fetchMySchedule();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Commit failed';
      showToast(msg);
    } finally {
      setCommittingCsv(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* ── Toast Notification ────────────────────────────────────────── */}
      {toastMessage && (
        <div className="fixed bottom-5 right-5 z-50 bg-slate-900 text-white px-4 py-2.5 rounded-xl text-xs font-semibold shadow-2xl flex items-center gap-2 animate-in fade-in slide-in-from-bottom-5">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* ── Header & Bell Status Radar ─────────────────────────────────── */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2.5">
            <div className="w-10 h-10 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center font-bold">
              <Clock className="w-5 h-5" />
            </div>
            <div>
              <h1 className="text-xl font-bold text-slate-900">Academic Timetable & Live Cockpit</h1>
              <p className="text-xs text-slate-500 mt-0.5">
                School bell schedules, classroom periods, spreadsheet ingestion, and real-time teacher tracking.
              </p>
            </div>
          </div>
        </div>

        {/* Live Clock & Active Bell Indicator */}
        <div className="flex items-center gap-3">
          <div className="px-3.5 py-2 rounded-xl bg-slate-50 border border-slate-200 flex items-center gap-2.5 text-xs">
            <div className="w-2.5 h-2.5 rounded-full bg-emerald-500 animate-pulse" />
            <div>
              <span className="text-slate-400 text-[10px] block">
                {currentTime.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
              </span>
              <span className="font-mono font-bold text-slate-800">
                {currentTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </span>
            </div>
            <div className="h-6 w-px bg-slate-200" />
            <div>
              <span className="text-slate-400 text-[10px] block">Active Bell</span>
              <span className="font-semibold text-blue-700">
                {activeSlot ? activeSlot.name : 'Out of Bell Hours'}
              </span>
            </div>
          </div>

          <button
            onClick={() => setActiveTab('UPLOAD')}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs shadow-md shadow-blue-500/20 transition-all"
          >
            <Upload className="w-3.5 h-3.5" />
            <span>Upload Timetable</span>
          </button>
        </div>
      </div>

      {/* ── Tabs Navigation ────────────────────────────────────────────── */}
      <div className="flex items-center gap-2 border-b border-slate-200 pb-2">
        <button
          onClick={() => setActiveTab('GRID')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'GRID'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <CalendarDays className="w-4 h-4" />
          <span>Class Weekly Schedule</span>
        </button>

        <button
          onClick={() => setActiveTab('LIVE')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'LIVE'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <Sparkles className="w-4 h-4" />
          <span>Live Class Radar & Substitutions</span>
        </button>

        <button
          onClick={() => setActiveTab('UPLOAD')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'UPLOAD'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <FileSpreadsheet className="w-4 h-4" />
          <span>Spreadsheet Ingest (CSV)</span>
        </button>

        <button
          onClick={() => setActiveTab('MY_SCHEDULE')}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition-all ${
            activeTab === 'MY_SCHEDULE'
              ? 'bg-blue-600 text-white shadow-xs'
              : 'text-slate-600 hover:text-slate-900 hover:bg-slate-100'
          }`}
        >
          <UserCheck className="w-4 h-4" />
          <span>My Daily Cockpit ({mySchedule.length} periods today)</span>
        </button>
      </div>

      {/* ══════════════════════════════════════════════════════════════════ */}
      {/* TAB 1: CLASS WEEKLY GRID                                           */}
      {/* ══════════════════════════════════════════════════════════════════ */}
      {activeTab === 'GRID' && (
        <div className="space-y-4">
          {/* Class & Section Filter Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 p-4 bg-white rounded-2xl border border-slate-200">
            <div className="flex items-center gap-3">
              <div>
                <label className="block text-[11px] font-semibold text-slate-500 mb-1">Select Class</label>
                <select
                  value={selectedClassId}
                  onChange={(e) => setSelectedClassId(e.target.value)}
                  className="px-3 py-1.5 border border-slate-200 rounded-xl text-xs font-bold text-slate-800 bg-white"
                >
                  {classes.map((c) => (
                    <option key={c.id} value={c.id}>
                      {c.name}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-[11px] font-semibold text-slate-500 mb-1">Section</label>
                <select
                  value={selectedSectionId}
                  onChange={(e) => setSelectedSectionId(e.target.value)}
                  className="px-3 py-1.5 border border-slate-200 rounded-xl text-xs font-bold text-slate-800 bg-white"
                >
                  {sections.map((sec) => (
                    <option key={sec.id} value={sec.id}>
                      Section {sec.name}
                    </option>
                  ))}
                </select>
              </div>

              {isPrimarySelected && (
                <div className="mt-4 px-3 py-1 bg-emerald-50 text-emerald-800 border border-emerald-200 rounded-xl text-[11px] font-semibold flex items-center gap-1.5">
                  <Sparkles className="w-3.5 h-3.5 text-emerald-600" />
                  <span>Homeroom Model (Nursery & Primary)</span>
                </div>
              )}
            </div>

            <div className="text-right text-xs text-slate-500">
              <span>Total Scheduled Slots: </span>
              <strong className="text-slate-800 font-mono">{timetableEntries.length}</strong>
            </div>
          </div>

          {/* Grid View */}
          {loadingGrid ? (
            <div className="p-12 text-center bg-white rounded-2xl border border-slate-200">
              <Loader2 className="w-6 h-6 animate-spin text-blue-600 mx-auto mb-2" />
              <p className="text-xs text-slate-500">Loading schedule grid...</p>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-slate-200 overflow-x-auto shadow-xs">
              <table className="w-full text-left border-collapse min-w-[900px]">
                <thead>
                  <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-600 uppercase tracking-wider">
                    <th className="py-3 px-4 w-28">Day</th>
                    {periodSlots.map((slot) => (
                      <th
                        key={slot.id}
                        className={`py-3 px-3 text-center border-l border-slate-200/80 ${
                          slot.isBreak ? 'bg-amber-50/50 text-amber-900 w-24' : ''
                        }`}
                      >
                        <div>{slot.name}</div>
                        <div className="text-[10px] text-slate-400 font-normal lowercase">
                          {slot.startTime.substring(0, 5)} - {slot.endTime.substring(0, 5)}
                        </div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100 text-xs">
                  {DAYS_OF_WEEK.map((day) => {
                    const isToday = day.id === currentDayIndex;
                    return (
                      <tr key={day.id} className={isToday ? 'bg-blue-50/20' : 'hover:bg-slate-50/40'}>
                        {/* Day Column */}
                        <td className="py-3.5 px-4 font-bold text-slate-800 flex items-center gap-1.5">
                          {isToday && <span className="w-2 h-2 rounded-full bg-blue-600" />}
                          <span>{day.name}</span>
                        </td>

                        {/* Period Slots */}
                        {periodSlots.map((slot) => {
                          if (slot.isBreak) {
                            return (
                              <td
                                key={slot.id}
                                className="py-3 px-2 text-center bg-amber-50/30 text-[11px] text-amber-700 italic border-l border-slate-100 font-medium"
                              >
                                {slot.name}
                              </td>
                            );
                          }

                          const entry = timetableEntries.find(
                            (e) => e.dayOfWeek === day.id && e.periodSlotId === slot.id
                          );

                          return (
                            <td
                              key={slot.id}
                              className="py-2.5 px-2.5 border-l border-slate-100 align-top max-w-[150px]"
                            >
                              {entry ? (
                                <div className="p-2 rounded-xl bg-slate-50 hover:bg-slate-100/80 border border-slate-200/80 space-y-1 relative group transition-all">
                                  {/* Subject / Homeroom Badge */}
                                  <div className="flex items-center justify-between">
                                    <span className="font-bold text-slate-900 text-xs truncate">
                                      {entry.subjectName || 'Homeroom'}
                                    </span>
                                    {entry.isSubstitution && (
                                      <span
                                        title={`Proxy: Originally ${entry.originalTeacherName || 'N/A'}`}
                                        className="text-[9px] font-extrabold px-1 rounded bg-amber-100 text-amber-800"
                                      >
                                        SUB
                                      </span>
                                    )}
                                  </div>

                                  {/* Teacher */}
                                  <div className="text-[11px] text-slate-600 truncate flex items-center gap-1">
                                    <UserCheck className="w-3 h-3 text-slate-400 shrink-0" />
                                    <span className="truncate">{entry.teacherName}</span>
                                  </div>

                                  {/* Room */}
                                  {entry.roomNumber && (
                                    <div className="text-[10px] text-slate-400 font-mono">
                                      {entry.roomNumber}
                                    </div>
                                  )}

                                  {/* Hover Actions: Proxy / Delete */}
                                  <div className="hidden group-hover:flex items-center gap-1 pt-1 border-t border-slate-200">
                                    <button
                                      onClick={() => handleOpenSubModal(entry)}
                                      title="Assign Proxy Substitute Teacher"
                                      className="px-1.5 py-0.5 rounded text-[10px] bg-blue-100 hover:bg-blue-200 text-blue-700 font-semibold"
                                    >
                                      Proxy
                                    </button>
                                    <button
                                      onClick={() => handleDeleteEntry(entry.id)}
                                      title="Remove scheduled class"
                                      className="p-1 rounded text-rose-500 hover:bg-rose-50 ml-auto"
                                    >
                                      <Trash2 className="w-3 h-3" />
                                    </button>
                                  </div>
                                </div>
                              ) : (
                                <button
                                  onClick={() => handleOpenScheduleModal(day.id, slot.id)}
                                  className="w-full h-16 rounded-xl border border-dashed border-slate-200 hover:border-blue-400 hover:bg-blue-50/50 flex flex-col items-center justify-center text-slate-400 hover:text-blue-600 transition-all text-[11px] gap-0.5"
                                >
                                  <Plus className="w-3.5 h-3.5" />
                                  <span>Schedule</span>
                                </button>
                              )}
                            </td>
                          );
                        })}
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════ */}
      {/* TAB 2: LIVE RADAR & PROXY SUBSTITUTIONS                             */}
      {/* ══════════════════════════════════════════════════════════════════ */}
      {activeTab === 'LIVE' && (
        <div className="space-y-4">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-base font-bold text-slate-900">
                  Active Bell Radar: {activeSlot ? activeSlot.name : 'Out of Bell Hours'}
                </h2>
                <p className="text-xs text-slate-500 mt-0.5">
                  Real-time classroom tracking: shows which teacher is currently lecturing and allows 1-click proxy replacement.
                </p>
              </div>
              <button
                onClick={fetchSectionTimetable}
                className="px-3 py-1.5 rounded-xl border border-slate-200 text-xs text-slate-600 hover:bg-slate-50 flex items-center gap-1.5"
              >
                <RefreshCw className="w-3.5 h-3.5" />
                <span>Refresh Radar</span>
              </button>
            </div>

            {/* Radar Cards for currently scheduled slots today */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 pt-2">
              {timetableEntries
                .filter((e) => e.dayOfWeek === currentDayIndex)
                .map((entry) => {
                  const isActive = activeSlot?.id === entry.periodSlotId;
                  return (
                    <div
                      key={entry.id}
                      className={`p-4 rounded-2xl border transition-all ${
                        isActive
                          ? 'border-blue-500 bg-blue-50/30 shadow-md ring-2 ring-blue-500/20'
                          : 'border-slate-200 bg-white hover:border-slate-300'
                      }`}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className="font-bold text-slate-900 text-sm">
                          {entry.className} - {entry.sectionName}
                        </span>
                        <span
                          className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                            isActive
                              ? 'bg-emerald-100 text-emerald-800 animate-pulse'
                              : 'bg-slate-100 text-slate-600'
                          }`}
                        >
                          {isActive ? '● IN PROGRESS' : entry.slotName}
                        </span>
                      </div>

                      <div className="space-y-1 text-xs text-slate-600 mb-3">
                        <div className="flex items-center gap-1.5">
                          <BookOpen className="w-3.5 h-3.5 text-blue-600" />
                          <span className="font-semibold text-slate-800">{entry.subjectName}</span>
                        </div>
                        <div className="flex items-center gap-1.5">
                          <UserCheck className="w-3.5 h-3.5 text-slate-400" />
                          <span>{entry.teacherName}</span>
                          {entry.isSubstitution && (
                            <span className="text-[10px] font-extrabold px-1 rounded bg-amber-100 text-amber-800">
                              PROXY
                            </span>
                          )}
                        </div>
                        <div className="flex items-center gap-1.5 font-mono text-[11px] text-slate-500">
                          <Building2 className="w-3.5 h-3.5 text-slate-400" />
                          <span>{entry.roomNumber || 'Default Room'}</span>
                        </div>
                      </div>

                      <div className="pt-2 border-t border-slate-100 flex items-center justify-between">
                        <button
                          onClick={() => handleOpenSubModal(entry)}
                          className="px-2.5 py-1 rounded-lg text-xs font-semibold bg-amber-50 hover:bg-amber-100 text-amber-800 border border-amber-200 flex items-center gap-1"
                        >
                          <UserPlus className="w-3 h-3" />
                          <span>Assign Substitute</span>
                        </button>
                        <Link
                          href={`/attendance?sectionId=${entry.sectionId}`}
                          className="text-xs font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1"
                        >
                          <span>Attendance</span>
                          <ArrowRight className="w-3 h-3" />
                        </Link>
                      </div>
                    </div>
                  );
                })}
            </div>
          </div>
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════ */}
      {/* TAB 3: SPREADSHEET INGESTION (CSV)                                 */}
      {/* ══════════════════════════════════════════════════════════════════ */}
      {activeTab === 'UPLOAD' && (
        <div className="bg-white p-6 rounded-2xl border border-slate-200 space-y-6">
          <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-4 border-b border-slate-100">
            <div>
              <h2 className="text-base font-bold text-slate-900">
                Bulk Timetable Spreadsheet Ingestion
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Upload your school's master schedule via CSV. The engine dry-runs the data for teacher collisions and room double-bookings before committing.
              </p>
            </div>
            <button
              onClick={handleDownloadTemplate}
              className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl border border-slate-200 hover:bg-slate-50 text-slate-700 text-xs font-semibold"
            >
              <Download className="w-3.5 h-3.5 text-slate-500" />
              <span>Download CSV Template</span>
            </button>
          </div>

          <div className="space-y-3">
            <label className="block text-xs font-semibold text-slate-700">
              Paste Timetable CSV Content or Drag-and-Drop:
            </label>
            <textarea
              rows={8}
              value={csvText}
              onChange={(e) => setCsvText(e.target.value)}
              placeholder={`Class,Section,DayOfWeek,PeriodNumber,SubjectCode,TeacherEmail,Room\nClass 5,A,Monday,1,,teacher@delhipublicacademy.edu,Room 101\nClass 6,A,Monday,1,MATH,teacher@delhipublicacademy.edu,Room 204`}
              className="w-full p-4 font-mono text-xs border border-slate-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500/20 bg-slate-50"
            />

            <div className="flex items-center justify-between pt-2">
              <span className="text-xs text-slate-500">
                Format: <code className="bg-slate-100 px-1 rounded text-slate-700">Class,Section,DayOfWeek,PeriodNumber,SubjectCode,TeacherEmail,Room</code>
              </span>
              <div className="flex items-center gap-2">
                <button
                  onClick={handleValidateCsv}
                  disabled={validatingCsv || !csvText.trim()}
                  className="px-4 py-2 bg-slate-900 hover:bg-black text-white text-xs font-semibold rounded-xl disabled:opacity-50 flex items-center gap-1.5"
                >
                  {validatingCsv && <Loader2 className="w-3 h-3 animate-spin" />}
                  <span>{validatingCsv ? 'Dry-Running...' : 'Run Pre-Flight Check'}</span>
                </button>
                {validationResult?.canCommit && (
                  <button
                    onClick={handleCommitCsv}
                    disabled={committingCsv}
                    className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white text-xs font-semibold rounded-xl shadow-md shadow-emerald-500/20 disabled:opacity-50 flex items-center gap-1.5"
                  >
                    {committingCsv && <Loader2 className="w-3 h-3 animate-spin" />}
                    <span>{committingCsv ? 'Importing...' : 'Commit & Apply Timetable'}</span>
                  </button>
                )}
              </div>
            </div>
          </div>

          {/* Dry Run Validation Report */}
          {validationResult && (
            <div className="p-4 rounded-xl border border-slate-200 space-y-3 bg-slate-50/50">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  {validationResult.canCommit ? (
                    <CheckCircle2 className="w-5 h-5 text-emerald-600" />
                  ) : (
                    <ShieldAlert className="w-5 h-5 text-rose-600" />
                  )}
                  <h3 className="text-xs font-bold text-slate-900">
                    Pre-Flight Report: {validationResult.validRows} / {validationResult.totalRows} valid entries
                  </h3>
                </div>
                <span
                  className={`px-2.5 py-0.5 rounded-full text-[10px] font-bold ${
                    validationResult.canCommit
                      ? 'bg-emerald-100 text-emerald-800'
                      : 'bg-rose-100 text-rose-800'
                  }`}
                >
                  {validationResult.canCommit ? 'PASSED (Ready to Commit)' : 'FAILED (Conflicts Found)'}
                </span>
              </div>

              {/* Conflict / Error List */}
              {validationResult.errors.length > 0 && (
                <div className="p-3 bg-rose-50 border border-rose-200 rounded-lg space-y-1 text-xs text-rose-800 font-medium">
                  {validationResult.errors.map((err, idx) => (
                    <div key={idx} className="flex items-start gap-1.5">
                      <span className="text-rose-500 font-bold">•</span>
                      <span>{err}</span>
                    </div>
                  ))}
                </div>
              )}

              {/* Preview Entries */}
              {validationResult.previewEntries.length > 0 && (
                <div className="overflow-x-auto max-h-60 rounded-lg border border-slate-200">
                  <table className="w-full text-left text-xs bg-white">
                    <thead className="bg-slate-50 text-[10px] font-bold text-slate-500 uppercase sticky top-0">
                      <tr>
                        <th className="py-2 px-3">Class</th>
                        <th className="py-2 px-3">Day</th>
                        <th className="py-2 px-3">Period</th>
                        <th className="py-2 px-3">Subject</th>
                        <th className="py-2 px-3">Teacher</th>
                        <th className="py-2 px-3">Room</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {validationResult.previewEntries.map((pe, idx) => (
                        <tr key={idx}>
                          <td className="py-2 px-3 font-semibold text-slate-800">
                            {pe.className} - {pe.sectionName}
                          </td>
                          <td className="py-2 px-3 text-slate-600">{pe.dayName}</td>
                          <td className="py-2 px-3 text-slate-600">Period {pe.slotNumber}</td>
                          <td className="py-2 px-3 text-slate-600">{pe.subjectName}</td>
                          <td className="py-2 px-3 text-slate-600">{pe.teacherName}</td>
                          <td className="py-2 px-3 font-mono text-[11px] text-slate-400">
                            {pe.roomNumber || '—'}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* ══════════════════════════════════════════════════════════════════ */}
      {/* TAB 4: MY DAILY COCKPIT (TEACHER VIEW)                             */}
      {/* ══════════════════════════════════════════════════════════════════ */}
      {activeTab === 'MY_SCHEDULE' && (
        <div className="bg-white p-6 rounded-2xl border border-slate-200 space-y-4">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100">
            <div>
              <h2 className="text-base font-bold text-slate-900">
                My Teaching Schedule for Today ({currentTime.toLocaleDateString('en-US', { weekday: 'long' })})
              </h2>
              <p className="text-xs text-slate-500 mt-0.5">
                Periods assigned to your user account in PostgreSQL. Click any class to jump straight into attendance.
              </p>
            </div>
          </div>

          {mySchedule.length === 0 ? (
            <div className="p-12 text-center text-slate-400">
              <CalendarDays className="w-8 h-8 mx-auto mb-2 text-slate-300" />
              <p className="text-xs">No scheduled teaching periods assigned to you today.</p>
            </div>
          ) : (
            <div className="divide-y divide-slate-100">
              {mySchedule.map((entry) => {
                const isActive = activeSlot?.id === entry.periodSlotId;
                return (
                  <div
                    key={entry.id}
                    className={`py-3.5 px-4 rounded-xl flex items-center justify-between transition-all ${
                      isActive ? 'bg-blue-50/60 border border-blue-200' : 'hover:bg-slate-50/60'
                    }`}
                  >
                    <div className="flex items-center gap-3">
                      <div className="w-12 text-center">
                        <span className="font-mono font-bold text-slate-800 text-sm block">
                          P{entry.slotNumber}
                        </span>
                        <span className="text-[10px] text-slate-400 block">
                          {entry.startTime.substring(0, 5)}
                        </span>
                      </div>

                      <div className="h-8 w-px bg-slate-200" />

                      <div>
                        <div className="flex items-center gap-2">
                          <span className="font-bold text-slate-900 text-sm">
                            {entry.className} - {entry.sectionName}
                          </span>
                          <span className="px-2 py-0.5 rounded bg-blue-100 text-blue-800 text-[10px] font-semibold">
                            {entry.subjectName}
                          </span>
                          {entry.isSubstitution && (
                            <span className="px-2 py-0.5 rounded bg-amber-100 text-amber-800 text-[10px] font-bold">
                              Proxy Lecture
                            </span>
                          )}
                        </div>
                        <div className="text-xs text-slate-500 flex items-center gap-2 mt-0.5">
                          <span>Room: {entry.roomNumber || 'Assigned Room'}</span>
                          <span>•</span>
                          <span>Time: {entry.startTime.substring(0, 5)} - {entry.endTime.substring(0, 5)}</span>
                        </div>
                      </div>
                    </div>

                    <Link
                      href={`/attendance?sectionId=${entry.sectionId}`}
                      className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white font-semibold text-xs rounded-xl shadow-xs flex items-center gap-1.5"
                    >
                      <span>Take Attendance</span>
                      <ArrowRight className="w-3.5 h-3.5" />
                    </Link>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      )}

      {/* ── Modal: Schedule Slot ─────────────────────────────────────────── */}
      {showAddModal && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-2xl w-full max-w-md p-6 shadow-2xl relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setShowAddModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <h2 className="text-base font-bold text-slate-900 mb-1">Schedule Classroom Period</h2>
            <p className="text-xs text-slate-500 mb-4">
              Assign teacher & subject for{' '}
              <strong className="text-slate-800">
                {DAYS_OF_WEEK.find((d) => d.id === modalDay)?.name} - Period{' '}
                {periodSlots.find((s) => s.id === modalSlotId)?.slotNumber}
              </strong>
            </p>

            <form onSubmit={handleSaveEntry} className="space-y-3.5 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Assign Teacher *</label>
                <select
                  required
                  value={modalTeacherId}
                  onChange={(e) => setModalTeacherId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl bg-white"
                >
                  <option value="">-- Choose Teacher --</option>
                  {staffList.map((s) => (
                    <option key={s.id} value={s.userId || s.id}>
                      {s.fullName} ({s.employeeId})
                    </option>
                  ))}
                </select>
              </div>

              {!isPrimarySelected ? (
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Subject *</label>
                  <select
                    required
                    value={modalSubjectId}
                    onChange={(e) => setModalSubjectId(e.target.value)}
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl bg-white"
                  >
                    <option value="">-- Select Subject --</option>
                    {classSubjects.map((sub) => (
                      <option key={sub.id} value={sub.id}>
                        {sub.name} ({sub.code || 'ACAD'})
                      </option>
                    ))}
                  </select>
                </div>
              ) : (
                <div className="p-2.5 bg-emerald-50 text-emerald-800 rounded-xl text-[11px] font-medium border border-emerald-200 flex items-center gap-2">
                  <Sparkles className="w-4 h-4 text-emerald-600 shrink-0" />
                  <span>Nursery/Primary: Handled by dedicated Homeroom Teacher.</span>
                </div>
              )}

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Room / Laboratory</label>
                <input
                  type="text"
                  value={modalRoom}
                  onChange={(e) => setModalRoom(e.target.value)}
                  placeholder="e.g. Room 204 or Science Lab 1"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl bg-white"
                />
              </div>

              <div className="pt-3 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowAddModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl text-slate-600 font-medium hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={savingEntry}
                  className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-semibold disabled:opacity-50 flex items-center gap-1.5"
                >
                  {savingEntry && <Loader2 className="w-3 h-3 animate-spin" />}
                  <span>{savingEntry ? 'Saving...' : 'Set Period'}</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Modal: Assign Proxy Substitute Teacher ──────────────────────── */}
      {showSubModal && targetEntryForSub && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-2xl w-full max-w-md p-6 shadow-2xl relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setShowSubModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-2 mb-1">
              <UserPlus className="w-5 h-5 text-amber-600" />
              <h2 className="text-base font-bold text-slate-900">Assign Proxy / Substitute</h2>
            </div>
            <p className="text-xs text-slate-500 mb-4">
              Designate a proxy teacher for{' '}
              <strong className="text-slate-800">
                {targetEntryForSub.className}-{targetEntryForSub.sectionName} (
                {targetEntryForSub.subjectName})
              </strong>{' '}
              originally handled by {targetEntryForSub.teacherName}.
            </p>

            <div className="space-y-3.5 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Select Substitute Teacher *
                </label>
                <select
                  value={selectedSubTeacherId}
                  onChange={(e) => setSelectedSubTeacherId(e.target.value)}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl bg-white font-medium"
                >
                  <option value="">-- Choose Available Teacher --</option>
                  {staffList
                    .filter((s) => Number(s.userId || s.id) !== Number(targetEntryForSub.teacherId))
                    .map((s) => (
                      <option key={s.id} value={s.userId || s.id}>
                        {s.fullName} ({s.designation || 'Teacher'})
                      </option>
                    ))}
                </select>
              </div>

              <div className="pt-3 flex justify-end gap-2 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setShowSubModal(false)}
                  className="px-4 py-2 border border-slate-200 rounded-xl text-slate-600 font-medium hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  disabled={savingSub || !selectedSubTeacherId}
                  onClick={handleSaveSubstitute}
                  className="px-5 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl font-semibold disabled:opacity-50 flex items-center gap-1.5"
                >
                  {savingSub && <Loader2 className="w-3 h-3 animate-spin" />}
                  <span>{savingSub ? 'Assigning...' : 'Confirm Substitution'}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
