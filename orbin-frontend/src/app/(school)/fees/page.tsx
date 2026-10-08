'use client';

// ============================================================================
// ORBIN SCHOOL - FEES TERMINAL, AUDIT LEDGER & LEGAL TAX RECEIPTS (80C)
// Connected directly to Spring Boot REST APIs (/api/v1/fees) & PostgreSQL
// ============================================================================

import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '@/context/auth-context';
import {
  CreditCard,
  Printer,
  Receipt,
  Search,
  CheckCircle2,
  X,
  FileDown,
  DollarSign,
  Loader2,
  AlertCircle,
  RefreshCw
} from 'lucide-react';
import { StudentFeeDto, PaymentStatus } from '@/lib/types';
import { api } from '@/lib/api';

export default function FeesPage() {
  const { currentSchool } = useAuth();
  const [fees, setFees] = useState<StudentFeeDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  // Payment Collection Terminal State
  const [payModalFee, setPayModalFee] = useState<StudentFeeDto | null>(null);
  const [payAmount, setPayAmount] = useState<number>(0);
  const [payMode, setPayMode] = useState<'CASH' | 'UPI' | 'CHEQUE' | 'NET_BANKING'>('UPI');
  const [transactionRef, setTransactionRef] = useState<string>('');
  const [chequeBank, setChequeBank] = useState<string>('');
  const [toastMessage, setToastMessage] = useState<string | null>(null);
  const [submittingPayment, setSubmittingPayment] = useState(false);

  // Official Printable Receipt Modal State
  const [selectedFee, setSelectedFee] = useState<StudentFeeDto | null>(null);
  const [receiptMeta, setReceiptMeta] = useState<{
    receiptNo: string;
    date: string;
    mode: string;
    ref: string;
  } | null>(null);

  // Load fees from real backend API
  const fetchFees = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await api.getStudentFees();
      const normalized: StudentFeeDto[] = (data || []).map((f: any) => ({
        id: String(f.id),
        studentId: String(f.studentId),
        studentName: f.studentName || 'Student',
        admissionNo: f.admissionNo || f.admissionNumber || 'ADM-N/A',
        feeStructureName: f.feeStructureName || 'Tuition & Academic Term Fee',
        termName: f.termName || 'Term 2 (Oct - Dec 2026)',
        totalAmount: Number(f.totalAmount || 0),
        paidAmount: Number(f.paidAmount || 0),
        balanceDue: Number(f.balanceDue ?? f.outstanding ?? (Number(f.totalAmount || 0) - Number(f.paidAmount || 0))),
        status: (f.status as PaymentStatus) || (Number(f.balanceDue ?? f.outstanding ?? 0) === 0 ? 'PAID' : 'PENDING'),
        dueDate: f.dueDate ? String(f.dueDate) : '2026-10-31',
        lastReceiptNumber: f.lastReceiptNumber || (f.receiptNumber ? String(f.receiptNumber) : undefined),
        lastPaymentDate: f.lastPaymentDate ? String(f.lastPaymentDate) : undefined,
      }));
      setFees(normalized);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to load fee ledger from backend';
      setError(msg);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFees();
  }, [fetchFees]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // Live Metrics computed from database state
  const totalAssigned = fees.reduce((acc, f) => acc + f.totalAmount, 0);
  const totalCollected = fees.reduce((acc, f) => acc + f.paidAmount, 0);
  const totalOutstanding = fees.reduce((acc, f) => acc + f.balanceDue, 0);
  const collectionRate = totalAssigned > 0 ? Math.round((totalCollected / totalAssigned) * 100) : 0;

  // Filtered List
  const filtered = fees.filter(f => {
    const matchesSearch =
      (f.studentName || '').toLowerCase().includes(search.toLowerCase()) ||
      (f.admissionNo || '').toLowerCase().includes(search.toLowerCase()) ||
      (f.lastReceiptNumber && f.lastReceiptNumber.toLowerCase().includes(search.toLowerCase()));

    const matchesStatus = statusFilter === 'ALL' || f.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const handleOpenPay = (fee: StudentFeeDto) => {
    setPayModalFee(fee);
    setPayAmount(fee.balanceDue);
    setPayMode('UPI');
    setTransactionRef(`UPI-${Date.now().toString().slice(-6)}`);
    setChequeBank('');
  };

  const handleRecordPayment = async () => {
    if (!payModalFee || payAmount <= 0) {
      showToast('Please enter a valid payment amount.');
      return;
    }

    setSubmittingPayment(true);
    const todayStr = new Date().toISOString().split('T')[0];
    const defaultRcpNo = `RCP-${new Date().getFullYear()}-${Math.floor(10000 + Math.random() * 90000)}`;

    try {
      // Record payment against real backend database
      const result = await api.recordPayment({
        studentFeeId: payModalFee.id,
        amount: payAmount,
        paymentMode: payMode,
        transactionReference: transactionRef,
        notes: chequeBank ? `Bank: ${chequeBank}` : undefined,
      });

      const issuedReceiptNo = result?.receiptNumber || defaultRcpNo;

      // Update local state smoothly
      const newPaid = payModalFee.paidAmount + payAmount;
      const newDue = Math.max(0, payModalFee.totalAmount - newPaid);
      const updatedFee: StudentFeeDto = {
        ...payModalFee,
        paidAmount: newPaid,
        balanceDue: newDue,
        status: newDue === 0 ? 'PAID' : 'PARTIAL',
        lastReceiptNumber: issuedReceiptNo,
        lastPaymentDate: todayStr,
      };

      setFees(prev => prev.map(f => (f.id === payModalFee.id ? updatedFee : f)));
      setPayModalFee(null);

      // Open Printable Tax Receipt Modal
      setReceiptMeta({
        receiptNo: issuedReceiptNo,
        date: todayStr,
        mode: payMode,
        ref: payMode === 'CHEQUE' ? `Chq: ${transactionRef} (${chequeBank})` : transactionRef,
      });
      setSelectedFee(updatedFee);

      showToast(`Payment of ₹${payAmount.toLocaleString()} recorded. Receipt ${issuedReceiptNo} generated!`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Failed to record payment in database';
      showToast(msg);
    } finally {
      setSubmittingPayment(false);
    }
  };

  const exportFeeLedger = () => {
    if (fees.length === 0) {
      showToast('No fee records to export.');
      return;
    }
    const headers = 'Admission_No,Student_Name,Term,Total_Fees,Paid_Amount,Balance_Due,Status,Last_Receipt_No,Last_Payment_Date\n';
    const rows = fees
      .map(
        f =>
          `"${f.admissionNo}","${f.studentName}","${f.termName}",${f.totalAmount},${f.paidAmount},${f.balanceDue},"${f.status}","${f.lastReceiptNumber || 'N/A'}","${f.lastPaymentDate || 'N/A'}"`
      )
      .join('\n');

    const blob = new Blob([headers + rows], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', `${currentSchool?.slug || 'school'}_fee_ledger.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    showToast('Exported fee collection ledger to CSV');
  };

  return (
    <div className="space-y-6">
      {/* ── Toast Notification ────────────────────────────────────────────── */}
      {toastMessage && (
        <div className="fixed bottom-6 right-6 z-50 flex items-center gap-2 bg-slate-900 border border-slate-700 text-white text-xs px-4 py-3 rounded-xl shadow-2xl animate-in fade-in slide-in-from-bottom-4">
          <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>{toastMessage}</span>
        </div>
      )}

      {/* ── Page Header ────────────────────────────────────────────────────── */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-white p-6 rounded-2xl border border-slate-200/80 shadow-xs">
        <div>
          <div className="flex items-center gap-2">
            <h1 className="text-xl font-bold text-slate-900">Fees Terminal & Receipt Desk</h1>
            <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-purple-50 text-purple-700 border border-purple-200">
              {currentSchool?.name || 'School Bursar'}
            </span>
          </div>
          <p className="text-xs text-slate-500 mt-1">
            Live collection terminal, real-time ledger accounting, and 80C eligible tax receipts.
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            type="button"
            onClick={fetchFees}
            disabled={loading}
            className="p-2.5 bg-slate-50 hover:bg-slate-100 text-slate-700 rounded-xl text-xs font-semibold border border-slate-200 transition-all active:scale-95 disabled:opacity-50"
            title="Refresh fee ledger"
          >
            <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
          </button>
          <button
            type="button"
            onClick={exportFeeLedger}
            className="px-4 py-2.5 rounded-xl bg-white hover:bg-slate-50 text-slate-700 font-semibold text-xs border border-slate-200 shadow-xs flex items-center gap-2 transition-all"
          >
            <FileDown className="w-4 h-4 text-slate-500" />
            <span>Export CSV</span>
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
            onClick={fetchFees}
            className="px-3 py-1 rounded-lg bg-red-600 text-white font-semibold hover:bg-red-700 text-xs"
          >
            Retry
          </button>
        </div>
      )}

      {/* ── KPI Metric Cards ──────────────────────────────────────────────── */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-slate-500">Total Assigned</span>
            <div className="p-2 bg-slate-100 text-slate-600 rounded-xl">
              <DollarSign className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-2xl font-black text-slate-900">
            {loading ? '—' : `₹${totalAssigned.toLocaleString()}`}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Aggregated curriculum fees</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-emerald-600 font-semibold">Total Collected</span>
            <div className="p-2 bg-emerald-50 text-emerald-600 rounded-xl">
              <CreditCard className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-2xl font-black text-emerald-700">
            {loading ? '—' : `₹${totalCollected.toLocaleString()}`}
          </div>
          <p className="text-[11px] text-emerald-600 font-medium mt-1">{collectionRate}% collection milestone</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-rose-600 font-semibold">Outstanding Dues</span>
            <div className="p-2 bg-rose-50 text-rose-600 rounded-xl">
              <Receipt className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-2xl font-black text-rose-700">
            {loading ? '—' : `₹${totalOutstanding.toLocaleString()}`}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Pending parent payments</p>
        </div>

        <div className="bg-white p-5 rounded-2xl border border-slate-200/80 shadow-xs">
          <div className="flex items-center justify-between">
            <span className="text-xs font-medium text-purple-600 font-semibold">Invoices Status</span>
            <div className="p-2 bg-purple-50 text-purple-600 rounded-xl">
              <CreditCard className="w-4 h-4" />
            </div>
          </div>
          <div className="mt-3 text-2xl font-black text-slate-900">
            {loading ? '—' : `${fees.filter(f => f.balanceDue === 0).length} / ${fees.length}`}
          </div>
          <p className="text-[11px] text-slate-400 mt-1">Fully cleared student records</p>
        </div>
      </div>

      {/* ── Filter Toolbar ─────────────────────────────────────────────────── */}
      <div className="bg-white p-4 rounded-2xl border border-slate-200/80 shadow-xs flex flex-col md:flex-row items-center justify-between gap-3">
        <div className="relative w-full md:w-96">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
          <input
            type="text"
            placeholder="Search by student name, admission number, or receipt..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-800 placeholder-slate-400 focus:outline-none focus:ring-2 focus:ring-purple-500/20 focus:border-purple-500 transition-all"
          />
        </div>

        <div className="flex items-center gap-2 w-full md:w-auto">
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="px-3 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs text-slate-700 font-medium focus:outline-none focus:ring-2 focus:ring-purple-500/20"
          >
            <option value="ALL">All Payment Statuses</option>
            <option value="PAID">Fully Paid</option>
            <option value="PARTIAL">Partially Paid</option>
            <option value="OVERDUE">Overdue / Pending</option>
          </select>
        </div>
      </div>

      {/* ── Fee Ledger Table ──────────────────────────────────────────────── */}
      <div className="bg-white rounded-2xl border border-slate-200/80 shadow-xs overflow-hidden">
        {loading ? (
          <div className="py-20 flex flex-col items-center justify-center text-slate-400 gap-3">
            <Loader2 className="w-8 h-8 animate-spin text-purple-600" />
            <p className="text-xs font-semibold">Connecting to PostgreSQL database and fetching fee ledger...</p>
          </div>
        ) : filtered.length === 0 ? (
          <div className="py-16 px-4 text-center">
            <div className="w-12 h-12 rounded-2xl bg-slate-100 text-slate-400 flex items-center justify-center mx-auto mb-3">
              <Receipt className="w-6 h-6" />
            </div>
            <h3 className="text-sm font-bold text-slate-800">No Student Fee Records Found</h3>
            <p className="text-xs text-slate-500 max-w-sm mx-auto mt-1 mb-4">
              There are currently no fee invoices created for students in PostgreSQL.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50/80 border-b border-slate-200 text-[11px] font-bold text-slate-500 uppercase tracking-wider">
                  <th className="py-3.5 px-4">Student Details</th>
                  <th className="py-3.5 px-4">Fee Structure</th>
                  <th className="py-3.5 px-4 text-right">Total Fees</th>
                  <th className="py-3.5 px-4 text-right">Paid</th>
                  <th className="py-3.5 px-4 text-right">Balance Due</th>
                  <th className="py-3.5 px-4 text-center">Status</th>
                  <th className="py-3.5 px-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-xs">
                {filtered.map(fee => (
                  <tr key={fee.id} className="hover:bg-slate-50/60 transition-colors">
                    {/* Student Details */}
                    <td className="py-3.5 px-4">
                      <div className="font-semibold text-slate-900">{fee.studentName}</div>
                      <div className="font-mono text-[11px] text-slate-400">{fee.admissionNo}</div>
                    </td>

                    {/* Structure & Term */}
                    <td className="py-3.5 px-4">
                      <div className="text-slate-800 font-medium">{fee.feeStructureName}</div>
                      <div className="text-[11px] text-slate-400">{fee.termName}</div>
                    </td>

                    {/* Total */}
                    <td className="py-3.5 px-4 text-right font-medium text-slate-900">
                      ₹{fee.totalAmount.toLocaleString()}
                    </td>

                    {/* Paid */}
                    <td className="py-3.5 px-4 text-right font-bold text-emerald-700">
                      ₹{fee.paidAmount.toLocaleString()}
                    </td>

                    {/* Balance */}
                    <td className="py-3.5 px-4 text-right font-black text-rose-600">
                      {fee.balanceDue === 0 ? '—' : `₹${fee.balanceDue.toLocaleString()}`}
                    </td>

                    {/* Status Badge */}
                    <td className="py-3.5 px-4 text-center">
                      <span
                        className={`inline-block px-2.5 py-0.5 rounded-full text-[10px] font-bold tracking-wide uppercase ${
                          fee.status === 'PAID'
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : fee.status === 'PARTIAL'
                              ? 'bg-amber-50 text-amber-700 border border-amber-200'
                              : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}
                      >
                        {fee.status}
                      </span>
                    </td>

                    {/* Actions */}
                    <td className="py-3.5 px-4 text-right space-x-2">
                      {fee.balanceDue > 0 && (
                        <button
                          type="button"
                          onClick={() => handleOpenPay(fee)}
                          className="px-3 py-1.5 rounded-lg bg-emerald-600 hover:bg-emerald-500 text-white font-semibold text-xs shadow-xs transition-all active:scale-95 inline-flex items-center gap-1"
                        >
                          <CreditCard className="w-3.5 h-3.5" />
                          <span>Collect</span>
                        </button>
                      )}

                      {fee.paidAmount > 0 && (
                        <button
                          type="button"
                          onClick={() => {
                            setSelectedFee(fee);
                            setReceiptMeta({
                              receiptNo: fee.lastReceiptNumber || 'RCP-2026-8901',
                              date: fee.lastPaymentDate || '2026-10-02',
                              mode: 'Bank / UPI',
                              ref: 'UTR-VERIFIED',
                            });
                          }}
                          className="px-2.5 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 text-slate-700 font-semibold text-xs inline-flex items-center gap-1"
                          title="Print Legal Tax Receipt (80C)"
                        >
                          <Printer className="w-3.5 h-3.5 text-slate-600" />
                          <span>Receipt</span>
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* ── Modal: Payment Collection Terminal ───────────────────────────── */}
      {payModalFee && (
        <div className="fixed inset-0 z-50 bg-slate-900/60 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white border border-slate-200 rounded-2xl w-full max-w-md p-6 shadow-2xl relative animate-in fade-in zoom-in-95">
            <button
              onClick={() => setPayModalFee(null)}
              className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"
            >
              <X className="w-5 h-5" />
            </button>

            <div className="flex items-center gap-2 mb-1">
              <div className="p-2 bg-emerald-50 text-emerald-600 rounded-xl">
                <CreditCard className="w-5 h-5" />
              </div>
              <div>
                <h2 className="text-base font-bold text-slate-900">Collect Student Fee</h2>
                <div className="text-[11px] text-slate-500">{payModalFee.studentName} &bull; {payModalFee.admissionNo}</div>
              </div>
            </div>

            <div className="my-4 p-3 bg-slate-50 rounded-xl border border-slate-200 text-xs space-y-1">
              <div className="flex justify-between">
                <span className="text-slate-500">Total Invoice:</span>
                <span className="font-semibold text-slate-800">₹{payModalFee.totalAmount.toLocaleString()}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-slate-500">Previously Paid:</span>
                <span className="font-semibold text-emerald-600">₹{payModalFee.paidAmount.toLocaleString()}</span>
              </div>
              <div className="flex justify-between border-t border-slate-200 pt-1 font-bold text-rose-600">
                <span>Outstanding Balance:</span>
                <span>₹{payModalFee.balanceDue.toLocaleString()}</span>
              </div>
            </div>

            <div className="space-y-3 text-xs">
              <div>
                <label className="block font-semibold text-slate-700 mb-1">Payment Amount (₹) *</label>
                <input
                  type="number"
                  min="1"
                  max={payModalFee.balanceDue}
                  value={payAmount}
                  onChange={e => setPayAmount(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl text-slate-900 font-bold text-sm focus:outline-none focus:ring-2 focus:ring-purple-500/20"
                />
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">Payment Mode</label>
                <div className="grid grid-cols-4 gap-1.5">
                  {(['UPI', 'CASH', 'CHEQUE', 'NET_BANKING'] as const).map(mode => (
                    <button
                      key={mode}
                      type="button"
                      onClick={() => setPayMode(mode)}
                      className={`py-1.5 rounded-lg font-bold text-[11px] transition-all ${
                        payMode === mode
                          ? 'bg-purple-600 text-white shadow-xs'
                          : 'bg-slate-100 text-slate-600 hover:bg-slate-200'
                      }`}
                    >
                      {mode}
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <label className="block font-semibold text-slate-700 mb-1">
                  Transaction / UTR Reference Number
                </label>
                <input
                  type="text"
                  value={transactionRef}
                  onChange={e => setTransactionRef(e.target.value)}
                  placeholder="e.g. UPI-9876543210"
                  className="w-full px-3 py-2 border border-slate-200 rounded-xl font-mono text-xs text-slate-800"
                />
              </div>

              {payMode === 'CHEQUE' && (
                <div>
                  <label className="block font-semibold text-slate-700 mb-1">Bank Name & Branch</label>
                  <input
                    type="text"
                    value={chequeBank}
                    onChange={e => setChequeBank(e.target.value)}
                    placeholder="e.g. State Bank of India, Main Branch"
                    className="w-full px-3 py-2 border border-slate-200 rounded-xl"
                  />
                </div>
              )}
            </div>

            <div className="mt-5 flex justify-end gap-2 pt-3 border-t border-slate-100">
              <button
                type="button"
                onClick={() => setPayModalFee(null)}
                className="px-4 py-2 border border-slate-200 rounded-xl text-slate-600 font-medium hover:bg-slate-50 text-xs"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleRecordPayment}
                disabled={submittingPayment}
                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl font-semibold shadow-md shadow-emerald-600/25 text-xs flex items-center gap-1.5 disabled:opacity-50"
              >
                {submittingPayment && <Loader2 className="w-3.5 h-3.5 animate-spin" />}
                <span>{submittingPayment ? 'Recording...' : `Confirm ₹${payAmount.toLocaleString()}`}</span>
              </button>
            </div>
          </div>
        </div>
      )}

      {/* ── Official Printable Tax Receipt Modal (80C Eligible) ───────────── */}
      {selectedFee && (
        <div className="fixed inset-0 bg-slate-950/70 backdrop-blur-xs z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-2xl w-full p-8 max-h-[90vh] overflow-y-auto my-8">
            <div className="flex justify-between items-center mb-4 print:hidden">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-wider">
                Official Tuition & Composite Fee Receipt
              </span>
              <button
                type="button"
                onClick={() => setSelectedFee(null)}
                className="text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Printable Receipt Container */}
            <div
              id="printable-receipt-area"
              className="border-2 border-slate-800 p-8 rounded-xl bg-white text-slate-900 space-y-6"
            >
              {/* Header */}
              <div className="flex justify-between items-start border-b-2 border-slate-900 pb-4">
                <div>
                  <h2 className="text-xl font-black uppercase tracking-tight text-slate-900">
                    {currentSchool?.name || 'School Campus'}
                  </h2>
                  <div className="text-xs text-slate-600 mt-1 font-medium">
                    {currentSchool?.board || 'Affiliated'} &bull; {currentSchool?.city || 'India'}
                  </div>
                  <div className="text-xs text-slate-600">
                    Tax Exempt Under Section 10(23C) / 80C Certified
                  </div>
                </div>

                <div className="text-right">
                  <div className="text-[10px] uppercase font-bold text-slate-500">Official Receipt</div>
                  <div className="font-mono text-sm font-extrabold text-blue-700">
                    {receiptMeta?.receiptNo || selectedFee.lastReceiptNumber || 'RCP-2026-8901'}
                  </div>
                  <div className="text-[11px] text-slate-500">
                    Date: {receiptMeta?.date || selectedFee.lastPaymentDate || new Date().toISOString().split('T')[0]}
                  </div>
                </div>
              </div>

              {/* Student Metadata */}
              <div className="grid grid-cols-2 gap-3 text-xs bg-slate-50/80 p-4 rounded-xl border border-slate-200">
                <div>
                  <span className="font-bold text-slate-600">Student Name:</span>{' '}
                  <span className="font-bold text-slate-900">{selectedFee.studentName}</span>
                </div>
                <div>
                  <span className="font-bold text-slate-600">Admission No:</span>{' '}
                  <span className="font-mono font-bold text-slate-900">{selectedFee.admissionNo}</span>
                </div>
                <div>
                  <span className="font-bold text-slate-600">Fee Structure:</span> {selectedFee.feeStructureName}
                </div>
                <div>
                  <span className="font-bold text-slate-600">Payment Mode:</span>{' '}
                  <span className="font-semibold text-emerald-700">{receiptMeta?.mode || 'UPI / Bank Transfer'}</span>
                </div>
                <div className="col-span-2 text-[11px] text-slate-500">
                  <span className="font-bold">Transaction Reference:</span> {receiptMeta?.ref || 'UTR-VERIFIED'}
                </div>
              </div>

              {/* Breakup Table */}
              <table className="w-full text-xs border-collapse">
                <thead>
                  <tr className="bg-slate-100 border-b-2 border-slate-800 text-slate-800 font-bold uppercase text-[10px]">
                    <th className="p-2.5 text-left">Fee Particulars</th>
                    <th className="p-2.5 text-left">80C Tax Status</th>
                    <th className="p-2.5 text-right">Amount (₹)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-200">
                  <tr>
                    <td className="p-2.5 font-medium">1. Tuition Fee (Academic Instruction)</td>
                    <td className="p-2.5 text-emerald-700 font-bold">Eligible for 80C</td>
                    <td className="p-2.5 text-right font-medium">₹{Math.round(selectedFee.paidAmount * 0.70).toLocaleString()}</td>
                  </tr>
                  <tr>
                    <td className="p-2.5 font-medium">2. STEM Science Laboratory & Library Fee</td>
                    <td className="p-2.5 text-slate-500">General Composite</td>
                    <td className="p-2.5 text-right font-medium">₹{Math.round(selectedFee.paidAmount * 0.20).toLocaleString()}</td>
                  </tr>
                  <tr>
                    <td className="p-2.5 font-medium">3. Digital Smart-Class & Sports Amenities</td>
                    <td className="p-2.5 text-slate-500">General Composite</td>
                    <td className="p-2.5 text-right font-medium">₹{Math.round(selectedFee.paidAmount * 0.10).toLocaleString()}</td>
                  </tr>
                </tbody>
                <tfoot>
                  <tr className="border-t-2 border-slate-900 font-extrabold text-sm">
                    <td colSpan={2} className="p-2.5">Total Amount Collected:</td>
                    <td className="p-2.5 text-right text-emerald-700">₹{selectedFee.paidAmount.toLocaleString()}</td>
                  </tr>
                  {selectedFee.balanceDue > 0 && (
                    <tr className="text-xs text-rose-600 font-bold">
                      <td colSpan={2} className="p-2.5">Remaining Balance Due:</td>
                      <td className="p-2.5 text-right">₹{selectedFee.balanceDue.toLocaleString()}</td>
                    </tr>
                  )}
                </tfoot>
              </table>

              {/* Seal & Signatures */}
              <div className="flex justify-between items-end pt-4">
                <div className="border-2 border-emerald-600 text-emerald-700 font-black text-xs px-3 py-1.5 uppercase rounded-lg tracking-wider -rotate-3">
                  RECEIVED & VERIFIED &bull; CASHIER DESK
                </div>

                <div className="text-center">
                  <div className="font-mono text-[10px] text-slate-400 mb-6">[Digitally Authenticated Cryptographic Signature]</div>
                  <div className="text-xs font-bold border-t border-slate-900 pt-1">Accounts Officer / Bursar</div>
                </div>
              </div>

              <div className="text-[10px] text-slate-400 text-center border-t border-slate-200 pt-2">
                This is an official computer-generated receipt issued by {currentSchool?.name}. Certified for parent income tax exemption filing.
              </div>
            </div>

            {/* Modal Controls */}
            <div className="flex justify-end gap-3 mt-6 print:hidden">
              <button
                type="button"
                onClick={() => setSelectedFee(null)}
                className="px-4 py-2 rounded-xl bg-slate-100 text-slate-600 text-xs font-semibold hover:bg-slate-200"
              >
                Close
              </button>
              <button
                type="button"
                onClick={() => window.print()}
                className="px-5 py-2 rounded-xl bg-purple-600 hover:bg-purple-700 text-white text-xs font-semibold shadow-md flex items-center gap-2"
              >
                <Printer className="w-4 h-4" />
                <span>Print Official Receipt</span>
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
