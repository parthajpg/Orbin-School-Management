// ============================================================================
// ORBIN SCHOOL - TYPESCRIPT DATA CONTRACTS (Synchronized with Spring Boot DTOs)
// ============================================================================

export type UserRole =
  | 'SUPER_ADMIN'
  | 'SCHOOL_ADMIN'
  | 'PRINCIPAL'
  | 'TEACHER'
  | 'ACCOUNTANT'
  | 'PARENT'
  | 'STUDENT';

export interface UserDto {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
  schoolId?: string;
  schoolName?: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserDto;
}

// ── Tenancy & School ────────────────────────────────────────────────────────
export interface SchoolBranding {
  primaryColor: string;
  secondaryColor: string;
  accentColor: string;
  logoUrl?: string;
  faviconUrl?: string;
}

export interface TenantSchool {
  id: string;
  name: string;
  slug: string;
  board: string;
  city: string;
  motto?: string;
  phone?: string;
  email?: string;
  address?: string;
  branding: SchoolBranding;
  activeModules: {
    attendance: boolean;
    fees: boolean;
    syllabus: boolean;
    exams: boolean;
    whatsapp: boolean;
    cms: boolean;
  };
}

// ── Academic Hierarchy ──────────────────────────────────────────────────────
export interface SchoolClass {
  id: string;
  name: string;
  numericLevel: number;
}

export interface Section {
  id: string;
  classId: string;
  name: string;
  roomNumber?: string;
  capacity?: number;
}

export interface Subject {
  id: string;
  name: string;
  code: string;
  icon?: string;
}

// ── Students ────────────────────────────────────────────────────────────────
export interface StudentResponse {
  id: string;
  admissionNo: string;
  rollNo: number;
  firstName: string;
  lastName: string;
  fullName: string;
  gender: string;
  dob: string;
  classId: string;
  className: string;
  sectionId: string;
  sectionName: string;
  parentName: string;
  parentPhone: string;
  parentEmail?: string;
  attendancePercentage?: number;
  status: 'ACTIVE' | 'INACTIVE' | 'GRADUATED';
}

export interface CreateStudentRequest {
  firstName: string;
  lastName: string;
  gender: string;
  dob: string;
  classId: string;
  sectionId: string;
  parentName: string;
  parentPhone: string;
  parentEmail?: string;
}

// ── Attendance ──────────────────────────────────────────────────────────────
export type AttendanceStatus = 'PRESENT' | 'ABSENT' | 'LATE' | 'EXCUSED';

export interface AttendanceRecordDto {
  id?: string;
  studentId: string;
  studentName: string;
  rollNo: number;
  admissionNo: string;
  status: AttendanceStatus;
  date: string;
  remarks?: string;
}

export interface MarkAttendanceItem {
  studentId: string;
  status: AttendanceStatus;
  remarks?: string;
}

export interface MarkAttendanceRequest {
  sectionId: string;
  date: string; // YYYY-MM-DD
  records: MarkAttendanceItem[];
}

export interface AttendanceStatsDto {
  totalStudents: number;
  presentCount: number;
  absentCount: number;
  lateCount: number;
  attendanceRate: number;
}

// ── Fees & Payments ─────────────────────────────────────────────────────────
export type PaymentStatus = 'PAID' | 'PARTIAL' | 'OVERDUE' | 'PENDING';

export interface StudentFeeDto {
  id: string;
  studentId: string;
  studentName: string;
  admissionNo: string;
  feeStructureName: string;
  termName: string;
  totalAmount: number;
  paidAmount: number;
  balanceDue: number;
  status: PaymentStatus;
  dueDate: string;
  lastPaymentDate?: string;
  lastReceiptNumber?: string;
}

export interface RecordPaymentRequest {
  studentFeeId: string;
  amount: number;
  paymentMode: 'CASH' | 'UPI' | 'NET_BANKING' | 'CHEQUE';
  transactionReference?: string;
  notes?: string;
}

export interface FeeReceiptDto {
  receiptNumber: string;
  paymentDate: string;
  studentName: string;
  admissionNo: string;
  className: string;
  amountPaid: number;
  balanceDue: number;
  paymentMode: string;
  schoolName: string;
  schoolAddress: string;
  schoolPhone: string;
  schoolBoard: string;
}

export interface FeeDashboardDto {
  totalAssigned: number;
  totalCollected: number;
  totalOutstanding: number;
  collectionRate: number;
  pendingInvoicesCount: number;
}

// ── Syllabus Progress ───────────────────────────────────────────────────────
export interface SyllabusTopicDto {
  id: string;
  name: string;
  completed: boolean;
  periodsEstimate?: number;
}

export interface SyllabusChapterDto {
  id: string;
  subjectId: string;
  chapterNumber: number;
  title: string;
  status: 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED';
  totalTopics: number;
  completedTopics: number;
  topics: SyllabusTopicDto[];
}

// ── Exams & Results ─────────────────────────────────────────────────────────
export interface TestDto {
  id: string;
  name: string;
  academicYearId: string;
  classId: string;
  subjectId: string;
  maxMarks: number;
  passingMarks: number;
  examDate: string;
}

export interface StudentMarksRow {
  studentId: string;
  studentName: string;
  rollNo: number;
  admissionNo: string;
  marksObtained: number;
  percentage: number;
  grade: string;
}

// ── Staff & Faculty Management ─────────────────────────────────────────────
export type StaffStatus = 'ACTIVE' | 'ON_LEAVE' | 'RESIGNED';

export interface StaffDto {
  id: string;
  employeeId: string;
  userId?: string;
  firstName: string;
  lastName: string;
  fullName: string;
  email: string;
  phone: string;
  role: UserRole;
  designation: string;
  department: string;
  assignedClassId?: string;
  assignedClassName?: string;
  assignedSectionId?: string;
  assignedSectionName?: string;
  subjectsTaught: string[];
  qualification: string;
  dateOfJoining: string;
  status: StaffStatus;
}

export interface CreateStaffRequest {
  employeeId?: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  role: UserRole;
  designation: string;
  department: string;
  assignedClassId?: string;
  assignedSectionId?: string;
  subjectsTaught: string[];
  qualification?: string;
  dateOfJoining?: string;
  initialPassword?: string;
}

// ── WhatsApp Transactional Notifications ───────────────────────────────────
export interface WhatsAppNotificationDto {
  id: string;
  recipientName: string;
  recipientPhone: string;
  studentId?: string;
  studentName?: string;
  templateType: string;
  messageContent: string;
  status: 'DELIVERED' | 'PENDING' | 'FAILED';
  metaMessageId?: string;
  sentAt: string;
}



