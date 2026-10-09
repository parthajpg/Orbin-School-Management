'use client';

// ============================================================================
// ORBIN SCHOOL - EXAMS & GRADEBOOK DESK
// Connected directly to Spring Boot REST APIs (/api/v1/exams) & PostgreSQL
// Strictly Zero Mock Data — 100% Real Relational Persistence
// ============================================================================

import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/auth-context';
import {
  GraduationCap,
  Award,
  Loader2,
  AlertCircle,
  RefreshCw,
  Plus,
  Save,
  CheckCircle2,
  X,
  Calendar,
  Layers,
  FileText
} from 'lucide-react';
import { api } from '@/lib/api';
import { StudentResponse, TestDto, StudentMarksRow, Section, SchoolClass, Subject } from '@/lib/types';

export default function ExamsPage() {
  const { currentSchool } = useAuth();

  // Academic hierarchy
  const [sections, setSections] = useState<Section[]>([]);
  const [selectedSectionId, setSelectedSectionId] = useState<string>('');
  const [subjects, setSubjects] = useState<Subject[]>([]);

  // Tests & Gradebook
  const [tests, setTests] = useState<TestDto[]>([]);
  const [selectedTest, setSelectedTest] = useState<TestDto | null>(null);
  const [students, setStudents] = useState<StudentResponse[]>([]);
  const [studentMarks, setStudentMarks] = useState<Record<string, number>>({});
  const [savedMarksMap, setSavedMarksMap] = useState<Record<string, StudentMarksRow>>({});

  // Loading & Feedback
  const [loading, setLoading] = useState(true);
  const [loadingTests, setLoadingTests] = useState(false);
  const [savingMarks, setSavingMarks] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Modals
  const [showScheduleModal, setShowScheduleModal] = useState(false);
  const [selectedStudentCard, setSelectedStudentCard] = useState<{
    student: StudentResponse;
    test: TestDto;
    marksObtained: number;
    maxMarks: number;
  } | null>(null);

  // New Test Form State
  const [newTestTitle, setNewTestTitle] = useState('');
  const [newTestSubjectId, setNewTestSubjectId] = useState('');
  const [newTestDate, setNewTestDate] = useState(() => new Date().toISOString().split('T')[0]);
  const [newTestMaxMarks, setNewTestMaxMarks] = useState('100');
  const [newTestDuration, setNewTestDuration] = useState('60');
  const [creatingTest, setCreatingTest] = useState(false);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // 1. Initial Load: Academic Classes & Sections
  useEffect(() => {
    async function loadAcademicStructure() {
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
          setSections(allSecs);
          if (allSecs.length > 0) {
            setSelectedSectionId(String(allSecs[0].id));
          }
        }
      } catch (err) {
        console.error('Failed to load classes/sections in exams', err);
      } finally {
        setLoading(false);
      }
    }
    loadAcademicStructure();
  }, []);

  // 2. Load Tests & Students when Section changes
  const loadSectionTestsAndStudents = useCallback(async (sectionId: string) => {
    if (!sectionId) return;
    setLoadingTests(true);
    setError(null);
    try {
      // Fetch students enrolled in this section
      const enrolled = await api.getStudents({ sectionId });
      setStudents(enrolled || []);

      // Fetch scheduled tests for this section
      const testList = await api.getExams(sectionId);
      setTests(testList || []);

      if (testList && testList.length > 0) {
        setSelectedTest(testList[0]);
      } else {
        setSelectedTest(null);
        setSavedMarksMap({});
        setStudentMarks({});
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load exams data';
      setError(msg);
    } finally {
      setLoadingTests(false);
    }
  }, []);

  useEffect(() => {
    if (selectedSectionId) {
      loadSectionTestsAndStudents(selectedSectionId);
    }
  }, [selectedSectionId, loadSectionTestsAndStudents]);

  // 3. Load Results when Selected Test changes
  useEffect(() => {
    async function loadTestResults() {
      if (!selectedTest) return;
      try {
        const results = await api.getExamMarks(selectedTest.id);
        const map: Record<string, StudentMarksRow> = {};
        const marksInput: Record<string, number> = {};

        results.forEach(r => {
          map[r.studentId] = r;
          marksInput[r.studentId] = r.marksObtained;
        });

        setSavedMarksMap(map);
        setStudentMarks(marksInput);
      } catch (err) {
        console.error('Failed to load marks for test', err);
      }
    }
    loadTestResults();
  }, [selectedTest]);

  // Save Marks to Backend (POST /api/v1/exams/marks)
  const handleSaveMarks = async () => {
    if (!selectedTest) return;
    setSavingMarks(true);
    setError(null);
    try {
      const marksPayload = students
        .filter(s => studentMarks[String(s.id)] !== undefined)
        .map(s => ({
          studentId: Number(s.id),
          marksObtained: Number(studentMarks[String(s.id)] || 0),
          remarks: 'Recorded via Faculty Gradebook'
        }));

      if (marksPayload.length === 0) {
        showToast('Please enter marks for at least one student before saving.');
        setSavingMarks(false);
        return;
      }

      await api.enterMarks({
        testId: Number(selectedTest.id),
        marks: marksPayload
      });

      // Refresh marks from DB
      const freshResults = await api.getExamMarks(selectedTest.id);
      const map: Record<string, StudentMarksRow> = {};
      freshResults.forEach(r => {
        map[r.studentId] = r;
      });
      setSavedMarksMap(map);

      showToast(`Successfully committed marks for ${marksPayload.length} students to PostgreSQL.`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to save marks to server';
      setError(msg);
    } finally {
      setSavingMarks(false);
    }
  };

  // Create New Test (POST /api/v1/exams)
  const handleCreateTest = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSectionId || !newTestTitle.trim()) return;

    setCreatingTest(true);
    setError(null);
    try {
      const created = await api.createTest({
        sectionId: Number(selectedSectionId),
        subjectId: Number(newTestSubjectId) || 1,
        title: newTestTitle.trim(),
        testDate: newTestDate,
        maxMarks: Number(newTestMaxMarks) || 100,
        durationMin: Number(newTestDuration) || 60,
        instructions: 'Official School Term Assessment'
      });

      showToast(`Scheduled test "${created.title}" successfully.`);
      setShowScheduleModal(false);
      setNewTestTitle('');

      // Reload tests
      await loadSectionTestsAndStudents(selectedSectionId);
      setSelectedTest(created);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to create exam';
      setError(msg);
    } finally {
      setCreatingTest(false);
    }
  };

  const getGrade = (pct: number) => {
    if (pct >= 90) return 'A+';
    if (pct >= 80) return 'A';
    if (pct >= 70) return 'B';
    if (pct >= 60) return 'C';
    if (pct >= 50) return 'D';
    return 'F';
  };

  return (
    <div className="space-y-6">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-5 right-5 z-50 bg-slate-900 text-white px-5 py-3 rounded-xl shadow-xl flex items-center gap-3 border border-slate-700 animate-slide-up text-xs font-semibold">
          <CheckCircle2 className="w-4 h-4 text-emerald-400" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900 tracking-tight flex items-center gap-2">
              <GraduationCap className="w-6 h-6 text-amber-600" />
              <span>Exams & Gradebook Desk</span>
            </h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-50 text-amber-700 border border-amber-200">
              {currentSchool?.name || 'School Campus'}{tests.length > 0 ? ` • ${tests.length} Exams` : ''}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Real-time multi-teacher assessment &bull; Direct PostgreSQL marks persistence
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={() => selectedSectionId && loadSectionTestsAndStudents(selectedSectionId)}
            disabled={loadingTests}
            className="p-2.5 bg-slate-50 hover:bg-slate-100 text-slate-700 rounded-xl text-xs font-semibold border border-slate-200 transition-all active:scale-95 disabled:opacity-50"
            title="Refresh gradebook"
          >
            <RefreshCw className={`w-4 h-4 ${loadingTests ? 'animate-spin' : ''}`} />
          </button>

          <button
            type="button"
            onClick={() => setShowScheduleModal(true)}
            className="px-4 py-2.5 bg-amber-600 hover:bg-amber-700 text-white font-semibold text-xs rounded-xl shadow-xs flex items-center gap-2 transition-all active:scale-95"
          >
            <Plus className="w-4 h-4" />
            <span>Schedule New Exam</span>
          </button>
        </div>
      </div>

      {/* Server Error Alert */}
      {error && (
        <div className="p-4 rounded-xl bg-red-50 border border-red-200 text-red-700 text-xs flex items-center justify-between gap-3">
          <div className="flex items-center gap-2">
            <AlertCircle className="w-4 h-4 text-red-600 shrink-0" />
            <span>{error}</span>
          </div>
          <button
            onClick={() => selectedSectionId && loadSectionTestsAndStudents(selectedSectionId)}
            className="px-3 py-1 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 text-xs"
          >
            Retry
          </button>
        </div>
      )}

      {/* Filter & Test Selector Bar */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-wrap items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3">
          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1">Class & Section</label>
            <select
              value={selectedSectionId}
              onChange={e => setSelectedSectionId(e.target.value)}
              className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
            >
              {sections.length === 0 ? (
                <option value="">No sections found</option>
              ) : (
                sections.map(s => (
                  <option key={s.id} value={s.id}>
                    {s.schoolClassName || s.className || 'Class'} - Section {s.name}
                  </option>
                ))
              )}
            </select>
          </div>

          <div>
            <label className="block text-[10px] font-bold uppercase text-slate-400 mb-1">Select Exam / Test</label>
            <select
              value={selectedTest ? String(selectedTest.id) : ''}
              onChange={e => {
                const found = tests.find(t => String(t.id) === e.target.value);
                setSelectedTest(found || null);
              }}
              disabled={tests.length === 0}
              className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20 disabled:opacity-50"
            >
              {tests.length === 0 ? (
                <option value="">No tests scheduled yet</option>
              ) : (
                tests.map(t => (
                  <option key={t.id} value={t.id}>
                    {t.title} ({t.subjectName || 'Subject'}) • Max: {t.maxMarks}
                  </option>
                ))
              )}
            </select>
          </div>
        </div>

        {selectedTest && students.length > 0 && (
          <button
            type="button"
            onClick={handleSaveMarks}
            disabled={savingMarks}
            className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white font-semibold text-xs rounded-xl shadow-xs flex items-center gap-2 transition-all active:scale-95 disabled:opacity-50"
          >
            {savingMarks ? (
              <Loader2 className="w-4 h-4 animate-spin" />
            ) : (
              <Save className="w-4 h-4" />
            )}
            <span>Commit Marks to Database</span>
          </button>
        )}
      </div>

      {/* Gradebook Matrix Table */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {loading || loadingTests ? (
          <div className="py-20 flex flex-col items-center justify-center text-slate-400 gap-3">
            <Loader2 className="w-8 h-8 animate-spin text-amber-600" />
            <p className="text-xs font-semibold">Connecting to PostgreSQL and loading exam roster...</p>
          </div>
        ) : !selectedTest ? (
          <div className="py-16 px-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-amber-50 text-amber-600 flex items-center justify-center mx-auto mb-3">
              <Calendar className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-slate-800">No Scheduled Exams Found</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1 mb-4">
              There are no examinations or unit tests scheduled for this section yet.
            </p>
            <button
              onClick={() => setShowScheduleModal(true)}
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold rounded-xl"
            >
              Schedule First Exam
            </button>
          </div>
        ) : students.length === 0 ? (
          <div className="py-16 px-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
              <GraduationCap className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-slate-800">No Enrolled Students</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1">
              Enroll students in this section to record marks and evaluate performance.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold uppercase text-slate-500 tracking-wider">
                  <th className="py-3 px-5">Roll #</th>
                  <th className="py-3 px-5">Student Information</th>
                  <th className="py-3 px-4 text-center">Marks Input (Max: {selectedTest.maxMarks})</th>
                  <th className="py-3 px-4 text-center">Percentage</th>
                  <th className="py-3 px-4 text-center">Grade</th>
                  <th className="py-3 px-4 text-center">Database Status</th>
                  <th className="py-3 px-5 text-right">Report</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {students.map((student, idx) => {
                  const sId = String(student.id);
                  const currentInput = studentMarks[sId] ?? 0;
                  const savedRecord = savedMarksMap[sId];
                  const maxM = selectedTest.maxMarks || 100;
                  const pct = Math.round((currentInput / maxM) * 100);
                  const grade = getGrade(pct);

                  return (
                    <tr key={sId} className="hover:bg-slate-50/60 transition-colors">
                      <td className="py-3.5 px-5 font-bold text-slate-700">#{student.rollNo || idx + 1}</td>
                      <td className="py-3.5 px-5">
                        <div className="font-bold text-slate-900">{student.fullName || `${student.firstName} ${student.lastName}`}</div>
                        <div className="font-mono text-[11px] text-slate-500">{student.admissionNo || `ADM-${student.id}`}</div>
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        <input
                          type="number"
                          min="0"
                          max={maxM}
                          value={studentMarks[sId] !== undefined ? studentMarks[sId] : ''}
                          placeholder="0"
                          onChange={e => {
                            const val = Math.min(maxM, Math.max(0, Number(e.target.value) || 0));
                            setStudentMarks(prev => ({ ...prev, [sId]: val }));
                          }}
                          className="w-24 text-center px-2 py-1 bg-slate-50 border border-slate-300 rounded-lg font-bold text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
                        />
                      </td>
                      <td className="py-3.5 px-4 text-center font-bold text-blue-700">{pct}%</td>
                      <td className="py-3.5 px-4 text-center">
                        <span className={`px-2 py-0.5 rounded font-black text-[11px] ${
                          grade === 'F'
                            ? 'bg-red-50 text-red-700 border border-red-200'
                            : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                        }`}>
                          {grade}
                        </span>
                      </td>
                      <td className="py-3.5 px-4 text-center">
                        {savedRecord ? (
                          <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                            Committed ({savedRecord.marksObtained})
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded text-[10px] font-semibold bg-amber-50 text-amber-700 border border-amber-200">
                            Unsaved
                          </span>
                        )}
                      </td>
                      <td className="py-3.5 px-5 text-right">
                        <button
                          type="button"
                          onClick={() => setSelectedStudentCard({
                            student,
                            test: selectedTest,
                            marksObtained: currentInput,
                            maxMarks: maxM
                          })}
                          className="px-2.5 py-1 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-[11px]"
                        >
                          View Card
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Schedule Exam Modal */}
      {showScheduleModal && (
        <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-md w-full p-6 relative">
            <button
              onClick={() => setShowScheduleModal(false)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>
            <h2 className="text-base font-bold text-slate-900 mb-1 flex items-center gap-2">
              <Plus className="w-5 h-5 text-amber-600" />
              <span>Schedule New Examination</span>
            </h2>
            <p className="text-xs text-slate-500 mb-5">
              Creates a live test record in PostgreSQL linked to the academic curriculum.
            </p>

            <form onSubmit={handleCreateTest} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Test Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Unit Test 1 - Algebra"
                  value={newTestTitle}
                  onChange={e => setNewTestTitle(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Test Date *</label>
                  <input
                    type="date"
                    required
                    value={newTestDate}
                    onChange={e => setNewTestDate(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
                  />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-700 mb-1">Max Marks *</label>
                  <input
                    type="number"
                    required
                    min="1"
                    max="500"
                    value={newTestMaxMarks}
                    onChange={e => setNewTestMaxMarks(e.target.value)}
                    className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-700 mb-1">Duration (Minutes)</label>
                <input
                  type="number"
                  min="15"
                  max="300"
                  value={newTestDuration}
                  onChange={e => setNewTestDuration(e.target.value)}
                  className="w-full px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-900 focus:outline-hidden focus:ring-2 focus:ring-amber-500/20"
                />
              </div>

              <div className="pt-2 flex justify-end gap-2.5">
                <button
                  type="button"
                  onClick={() => setShowScheduleModal(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-slate-600 hover:bg-slate-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creatingTest}
                  className="px-5 py-2 bg-amber-600 hover:bg-amber-700 text-white rounded-xl text-xs font-semibold shadow-xs flex items-center gap-2 disabled:opacity-50"
                >
                  {creatingTest ? <Loader2 className="w-4 h-4 animate-spin" /> : <Plus className="w-4 h-4" />}
                  <span>Schedule Test</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Official Report Card Modal */}
      {selectedStudentCard && (
        <div className="fixed inset-0 z-50 bg-slate-950/70 backdrop-blur-xs flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-xl w-full p-8 relative my-8">
            <button
              onClick={() => setSelectedStudentCard(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="border-4 border-slate-900 p-6 rounded-xl space-y-6">
              <div className="text-center border-b-2 border-slate-900 pb-4">
                <h2 className="text-xl font-black uppercase text-slate-900">
                  {currentSchool?.name || 'School Campus'}
                </h2>
                <div className="text-xs text-slate-600 mt-0.5 font-medium">
                  {currentSchool?.board || 'Affiliated'} &bull; Academic Session 2026-27
                </div>
                <div className="text-[11px] font-bold text-amber-700 uppercase tracking-widest mt-1">
                  Official Statement of Marks & Evaluation
                </div>
              </div>

              <div className="grid grid-cols-2 gap-2 text-xs bg-slate-50 p-3 rounded-lg border border-slate-200">
                <div>
                  <span className="font-bold text-slate-600">Student Name:</span>{' '}
                  <span className="font-extrabold text-slate-900">
                    {selectedStudentCard.student.fullName || `${selectedStudentCard.student.firstName} ${selectedStudentCard.student.lastName}`}
                  </span>
                </div>
                <div>
                  <span className="font-bold text-slate-600">Admission No:</span>{' '}
                  <span className="font-mono text-slate-900">{selectedStudentCard.student.admissionNo || `ADM-${selectedStudentCard.student.id}`}</span>
                </div>
                <div>
                  <span className="font-bold text-slate-600">Examination:</span>{' '}
                  <span className="font-bold text-slate-900">{selectedStudentCard.test.title}</span>
                </div>
                <div>
                  <span className="font-bold text-slate-600">Date of Test:</span>{' '}
                  <span className="font-mono text-slate-900">{selectedStudentCard.test.testDate || 'N/A'}</span>
                </div>
              </div>

              <div className="border border-slate-300 rounded-lg overflow-hidden">
                <table className="w-full text-xs">
                  <thead className="bg-slate-100 border-b border-slate-300 font-bold text-slate-700">
                    <tr>
                      <th className="py-2.5 px-4 text-left">Subject / Component</th>
                      <th className="py-2.5 px-4 text-center">Maximum Marks</th>
                      <th className="py-2.5 px-4 text-center">Marks Obtained</th>
                      <th className="py-2.5 px-4 text-center">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-200">
                    <tr>
                      <td className="py-2.5 px-4 font-semibold text-slate-900">
                        {selectedStudentCard.test.subjectName || selectedStudentCard.test.title}
                      </td>
                      <td className="py-2.5 px-4 text-center">{selectedStudentCard.maxMarks}</td>
                      <td className="py-2.5 px-4 text-center font-bold text-slate-900">
                        {selectedStudentCard.marksObtained}
                      </td>
                      <td className="py-2.5 px-4 text-center">
                        <span className="px-2 py-0.5 rounded font-bold text-[10px] bg-emerald-50 text-emerald-700 border border-emerald-200">
                          {getGrade(Math.round((selectedStudentCard.marksObtained / selectedStudentCard.maxMarks) * 100))}
                        </span>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div className="flex justify-between items-center text-xs border-t-2 border-slate-900 pt-4 font-bold">
                <div>
                  Overall Percentage:{' '}
                  <span className="text-blue-700 font-black">
                    {Math.round((selectedStudentCard.marksObtained / selectedStudentCard.maxMarks) * 100)}%
                  </span>
                </div>
                <div>
                  Evaluation Grade:{' '}
                  <span className="text-emerald-700 font-black">
                    {getGrade(Math.round((selectedStudentCard.marksObtained / selectedStudentCard.maxMarks) * 100))}
                  </span>
                </div>
              </div>

              <div className="flex justify-between items-end pt-6 text-[11px] text-slate-500">
                <div className="text-center">
                  <div className="w-24 border-b border-slate-400 mb-1"></div>
                  <span>Class Teacher</span>
                </div>
                <div className="text-center">
                  <div className="w-24 border-b border-slate-400 mb-1"></div>
                  <span>Principal / Headmaster</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
