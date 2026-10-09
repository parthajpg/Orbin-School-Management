// ============================================================================
// ORBIN SCHOOL - TYPE-SAFE API CLIENT WITH JWT INTERCEPTOR
// ============================================================================

import {
  AuthResponse,
  TenantSchool,
  SchoolClass,
  Section,
  Subject,
  StudentResponse,
  CreateStudentRequest,
  AttendanceRecordDto,
  AttendanceStatsDto,
  StudentFeeDto,
  RecordPaymentRequest,
  FeeReceiptDto,
  FeeDashboardDto,
  SyllabusChapterDto,
  TestDto,
  StudentMarksRow,
  StaffDto,
  CreateStaffRequest,
  WhatsAppNotificationDto,
  PeriodSlotDto,
  TimetableEntryDto,
  CreateTimetableEntryRequest,
  TimetableImportRow,
  TimetableValidationResult
} from './types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

class ApiClient {
  private getAccessToken(): string | null {
    if (typeof window === 'undefined') return null;
    return localStorage.getItem('orbin_access_token');
  }

  private setTokens(access: string, refresh: string) {
    if (typeof window === 'undefined') return;
    localStorage.setItem('orbin_access_token', access);
    localStorage.setItem('orbin_refresh_token', refresh);
  }

  private unwrapList<T>(res: any): T[] {
    if (Array.isArray(res)) return res;
    if (res && Array.isArray(res.content)) return res.content;
    return [];
  }

  private async request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
    const url = `${API_BASE_URL}${endpoint}`;
    const token = this.getAccessToken();

    const headers: Record<string, string> = {
      'Content-Type': 'application/json',
      ...(options.headers as Record<string, string>),
    };

    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    try {
      const response = await fetch(url, {
        ...options,
        headers,
      });

      // Handle 401 Unauthorized - Attempt Token Refresh
      if (response.status === 401 && typeof window !== 'undefined') {
        const refreshed = await this.refreshToken();
        if (refreshed) {
          headers['Authorization'] = `Bearer ${this.getAccessToken()}`;
          const retryResponse = await fetch(url, { ...options, headers });
          if (retryResponse.ok) {
            const data = await retryResponse.json();
            return (data.data !== undefined ? data.data : data) as T;
          }
        }
        localStorage.removeItem('orbin_access_token');
        localStorage.removeItem('orbin_user');
      }

      if (!response.ok) {
        const errorJson = await response.json().catch(() => ({}));
        throw new Error(errorJson.message || `API Error: ${response.status} ${response.statusText}`);
      }

      const json = await response.json();
      return (json.data !== undefined ? json.data : json) as T;
    } catch (err: unknown) {
      // Re-throw for caller handling
      throw err;
    }
  }

  private async refreshToken(): Promise<boolean> {
    const refreshToken = localStorage.getItem('orbin_refresh_token');
    if (!refreshToken) return false;

    try {
      const res = await fetch(`${API_BASE_URL}/auth/refresh`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken }),
      });

      if (res.ok) {
        const json = await res.json();
        const data: AuthResponse = json.data || json;
        this.setTokens(data.accessToken, data.refreshToken);
        return true;
      }
    } catch {
      // Refresh failed
    }
    return false;
  }

  // ── Auth Endpoints ────────────────────────────────────────────────────────
  async login(email: string, password: string): Promise<AuthResponse> {
    return this.request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
  }

  // ── Academic Endpoints ────────────────────────────────────────────────────
  async getClasses(): Promise<SchoolClass[]> {
    return this.request<SchoolClass[]>('/academic/classes');
  }

  async getSections(classId: string): Promise<Section[]> {
    return this.request<Section[]>(`/academic/classes/${classId}/sections`);
  }

  async getSubjects(classId?: string): Promise<Subject[]> {
    const qs = classId ? `?classId=${classId}` : '';
    return this.request<Subject[]>(`/academic/subjects${qs}`);
  }

  // ── Timetable Endpoints ───────────────────────────────────────────────────
  async getPeriodSlots(): Promise<PeriodSlotDto[]> {
    return this.request<PeriodSlotDto[]>('/academic/timetable/slots');
  }

  async getTimetableForSection(sectionId: string | number): Promise<TimetableEntryDto[]> {
    return this.request<TimetableEntryDto[]>(`/academic/timetable/section/${sectionId}`);
  }

  async getTimetableForTeacher(teacherId: string | number, dayOfWeek?: number): Promise<TimetableEntryDto[]> {
    const qs = dayOfWeek ? `?dayOfWeek=${dayOfWeek}` : '';
    return this.request<TimetableEntryDto[]>(`/academic/timetable/teacher/${teacherId}${qs}`);
  }

  async getMyScheduleToday(): Promise<TimetableEntryDto[]> {
    return this.request<TimetableEntryDto[]>('/academic/timetable/my-schedule');
  }

  async createTimetableEntry(payload: CreateTimetableEntryRequest): Promise<TimetableEntryDto> {
    return this.request<TimetableEntryDto>('/academic/timetable', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async validateTimetableImport(rows: TimetableImportRow[]): Promise<TimetableValidationResult> {
    return this.request<TimetableValidationResult>('/academic/timetable/validate-import', {
      method: 'POST',
      body: JSON.stringify(rows),
    });
  }

  async commitTimetableImport(rows: TimetableImportRow[]): Promise<TimetableValidationResult> {
    return this.request<TimetableValidationResult>('/academic/timetable/commit-import', {
      method: 'POST',
      body: JSON.stringify(rows),
    });
  }

  async assignSubstitute(entryId: number, substituteTeacherId: number): Promise<TimetableEntryDto> {
    return this.request<TimetableEntryDto>(`/academic/timetable/${entryId}/substitute`, {
      method: 'POST',
      body: JSON.stringify({ substituteTeacherId }),
    });
  }

  async deleteTimetableEntry(entryId: number): Promise<void> {
    return this.request<void>(`/academic/timetable/${entryId}`, {
      method: 'DELETE',
    });
  }

  // ── Students Endpoints ────────────────────────────────────────────────────
  async getStudents(params?: { classId?: string; sectionId?: string; search?: string }): Promise<StudentResponse[]> {
    const query = new URLSearchParams();
    if (params?.classId) query.append('classId', params.classId);
    if (params?.sectionId) query.append('sectionId', params.sectionId);
    if (params?.search) {
      query.append('search', params.search);
      query.append('query', params.search);
    }
    const qs = query.toString() ? `?${query.toString()}` : '';
    const res = await this.request<any>(`/students${qs}`);
    return this.unwrapList<StudentResponse>(res);
  }

  async createStudent(payload: CreateStudentRequest): Promise<StudentResponse> {
    return this.request<StudentResponse>('/students', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  // ── Attendance Endpoints ──────────────────────────────────────────────────
  async getAttendance(sectionId: string | number, date: string): Promise<AttendanceRecordDto[]> {
    const res = await this.request<any>(`/attendance/section/${sectionId}?date=${date}`);
    return Array.isArray(res) ? res : (res?.content || []);
  }

  async markAttendance(payload: {
    sectionId: string | number;
    date: string;
    records?: Array<{ studentId: string | number; status: string; remarks?: string }>;
    entries?: Array<{ studentId: string | number; status: string; notes?: string }>;
  }): Promise<AttendanceRecordDto[]> {
    const rawList = payload.entries || payload.records || [];
    const entries = rawList.map(item => ({
      studentId: Number(item.studentId) || item.studentId,
      status: item.status,
      notes: ('remarks' in item ? item.remarks : ('notes' in item ? item.notes : '')) || ''
    }));

    const body = {
      sectionId: Number(payload.sectionId) || 1,
      date: payload.date,
      entries
    };

    return this.request<AttendanceRecordDto[]>('/attendance/mark', {
      method: 'POST',
      body: JSON.stringify(body),
    });
  }

  async getAttendanceStats(sectionId: string | number, date: string): Promise<AttendanceStatsDto> {
    return this.request<AttendanceStatsDto>(`/attendance/stats/section/${sectionId}?date=${date}`);
  }

  // ── Fees Endpoints ────────────────────────────────────────────────────────
  async getStudentFees(params?: { classId?: string; status?: string }): Promise<StudentFeeDto[]> {
    const query = new URLSearchParams();
    if (params?.classId) query.append('classId', params.classId);
    if (params?.status) query.append('status', params.status);
    const qs = query.toString() ? `?${query.toString()}` : '';
    return this.request<StudentFeeDto[]>(`/fees/students${qs}`);
  }

  async recordPayment(payload: RecordPaymentRequest): Promise<FeeReceiptDto> {
    return this.request<FeeReceiptDto>('/fees/payments', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async getFeeDashboard(): Promise<FeeDashboardDto> {
    return this.request<FeeDashboardDto>('/fees/dashboard');
  }

  // ── Syllabus Endpoints ────────────────────────────────────────────────────
  async getSyllabus(subjectId: string | number): Promise<SyllabusChapterDto[]> {
    const id = Number(subjectId) || 1;
    const res = await this.request<any>(`/syllabus/chapters?subjectId=${id}`);
    const rawList = Array.isArray(res) ? res : (res?.content || []);
    return rawList.map((ch: any) => ({
      id: String(ch.id),
      subjectId: String(ch.subjectId || id),
      chapterNumber: ch.displayOrder || 1,
      title: ch.title,
      status: (ch.status || 'IN_PROGRESS') as 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED',
      totalTopics: (ch.topics || []).length,
      completedTopics: (ch.topics || []).filter((t: any) => t.completed).length,
      topics: (ch.topics || []).map((t: any) => ({
        id: String(t.id),
        name: t.title || t.name,
        completed: Boolean(t.completed),
        periodsEstimate: t.periodsEstimate || 2,
      })),
    }));
  }

  async toggleTopic(chapterId: string | number, sectionId: string | number, status: string): Promise<void> {
    return this.request<void>(`/syllabus/progress`, {
      method: 'POST',
      body: JSON.stringify({
        chapterId: Number(chapterId) || 1,
        sectionId: Number(sectionId) || 1,
        status: status || 'COMPLETED',
      }),
    });
  }

  // ── Exams Endpoints ───────────────────────────────────────────────────────
  async getExams(sectionId?: string | number): Promise<TestDto[]> {
    const sec = sectionId ? Number(sectionId) || 1 : 1;
    const res = await this.request<any>(`/exams/section/${sec}`);
    return Array.isArray(res) ? res : (res?.content || []);
  }

  async getExamMarks(testId: string | number): Promise<StudentMarksRow[]> {
    const id = Number(testId) || 1;
    const res = await this.request<any>(`/exams/${id}/results`);
    const rawList = Array.isArray(res) ? res : (res?.content || []);
    return rawList.map((r: any) => ({
      studentId: String(r.studentId),
      studentName: r.studentName || 'Student',
      rollNo: Number(r.studentId) || 1,
      admissionNo: r.admissionNumber || `ADM-${r.studentId}`,
      marksObtained: Number(r.marksObtained || 0),
      percentage: Number(r.percentage || 0),
      grade: Number(r.percentage || 0) >= 90 ? 'A+' : Number(r.percentage || 0) >= 80 ? 'A' : Number(r.percentage || 0) >= 70 ? 'B' : 'C',
    }));
  }

  async createTest(payload: {
    sectionId: number;
    subjectId: number;
    title: string;
    testDate: string;
    durationMin?: number;
    maxMarks: number;
    instructions?: string;
  }): Promise<TestDto> {
    return this.request<TestDto>('/exams', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async enterMarks(payload: {
    testId: number;
    marks: Array<{
      studentId: number;
      marksObtained: number;
      remarks?: string;
    }>;
  }): Promise<any[]> {
    return this.request<any[]>('/exams/marks', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  // ── Staff & Faculty Endpoints ─────────────────────────────────────────────
  async getStaffList(): Promise<StaffDto[]> {
    return this.request<StaffDto[]>('/staff');
  }

  async createStaff(payload: CreateStaffRequest): Promise<StaffDto> {
    return this.request<StaffDto>('/staff', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }

  async updateStaff(staffId: string, payload: Partial<CreateStaffRequest>): Promise<StaffDto> {
    return this.request<StaffDto>(`/staff/${staffId}`, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });
  }

  async resetStaffPassword(staffId: string): Promise<{ temporaryPassword: string; message: string }> {
    return this.request<{ temporaryPassword: string; message: string }>(`/staff/${staffId}/reset-credentials`, {
      method: 'POST',
    });
  }

  // ── WhatsApp Transactional Notifications ───────────────────────────────────
  async getWhatsAppLogs(): Promise<WhatsAppNotificationDto[]> {
    return this.request<WhatsAppNotificationDto[]>('/whatsapp/logs');
  }

  async sendAbsenceBlast(payload: {
    templateType?: string;
    recipients: Array<{
      studentId?: string | number;
      studentName: string;
      parentName: string;
      parentPhone: string;
      customMessage?: string;
    }>;
  }): Promise<WhatsAppNotificationDto[]> {
    const recipients = (payload.recipients || []).map(r => ({
      ...r,
      studentId: r.studentId ? (Number(r.studentId) || null) : null
    }));

    return this.request<WhatsAppNotificationDto[]>('/whatsapp/send-absence-blast', {
      method: 'POST',
      body: JSON.stringify({
        templateType: payload.templateType || 'ABSENCE_ALERT',
        recipients
      }),
    });
  }

  // ── Platform Super Admin Endpoints ────────────────────────────────────────
  async getTenantSchools(): Promise<TenantSchool[]> {
    const res = await this.request<any>('/schools');
    return Array.isArray(res) ? res : (res?.content || []);
  }

  async getCurrentSchool(): Promise<TenantSchool> {
    return this.request<TenantSchool>('/schools/current');
  }

  async onboardSchool(payload: Partial<TenantSchool>): Promise<TenantSchool> {
    return this.request<TenantSchool>('/schools', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  }
}

export const api = new ApiClient();
