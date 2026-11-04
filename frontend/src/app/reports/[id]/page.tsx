'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { api, downloadReportText, type Report } from '@/lib/api';
import { formatDate } from '@/lib/format';
import { StatusBadge, ReviewBadge } from '@/components/StatusBadge';
import { ArrowLeftIcon, CheckIcon, CopyIcon, DownloadIcon, AlertIcon, SpinnerIcon } from '@/components/icons';

export default function ReportDetail() {
  const { id } = useParams<{ id: string }>();
  const [report, setReport] = useState<Report | null>(null);
  const [editText, setEditText] = useState('');
  const [isEditing, setIsEditing] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    api.getReport(id)
      .then((data) => {
        const r = data as Report;
        setReport(r);
        setEditText(r.finalReport);
      });
  }, [id]);

  async function save() {
    const updated = await api.updateReport(id, editText) as Report;
    setReport(updated);
    setIsEditing(false);
    setMessage('Saved');
  }

  async function approve() {
    const updated = await api.approveReport(id) as Report;
    setReport(updated);
    setMessage('Report approved');
  }

  function copyToClipboard() {
    navigator.clipboard.writeText(report?.finalReport ?? '');
    setMessage('Copied to clipboard');
  }

  if (!report) {
    return (
      <main className="min-h-screen p-4 flex items-center justify-center">
        <p className="flex items-center gap-2 text-stone-500">
          <SpinnerIcon size={18} /> Loading report…
        </p>
      </main>
    );
  }

  return (
    <main className="min-h-screen p-4">
      <section className="max-w-3xl mx-auto space-y-4">

        {/* Header */}
        <div className="space-y-1">
          <Link href="/dashboard" className="link-muted">
            <ArrowLeftIcon size={16} /> Jobs
          </Link>
          <div className="flex items-center gap-2 flex-wrap pt-1">
            <StatusBadge status={report.status} />
            {report.lowConfidence && <ReviewBadge />}
            <span className="text-xs text-stone-500">{formatDate(report.createdAt, true)}</span>
          </div>
          <h1 className="text-2xl font-black tracking-tight">Site Report</h1>
        </div>

        {/* Low confidence warning */}
        {report.lowConfidence && (
          <div className="flex items-start gap-2 p-3 rounded-2xl bg-orange-50 border border-orange-200 text-orange-900 text-sm font-medium">
            <AlertIcon size={18} className="shrink-0 mt-0.5" />
            <span>The AI was uncertain about some details in this recording. Please read carefully and correct anything before approving.</span>
          </div>
        )}

        {/* Report body */}
        <div className="card p-5">
          {isEditing ? (
            <textarea
              className="input min-h-96 font-mono text-sm"
              value={editText}
              onChange={e => setEditText(e.target.value)}
              autoFocus
            />
          ) : (
            <pre className="whitespace-pre-wrap font-sans text-sm leading-relaxed text-stone-800">
              {report.finalReport}
            </pre>
          )}
        </div>

        {/* Actions */}
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {isEditing ? (
            <>
              <button onClick={save} className="btn btn-accent col-span-2 sm:col-span-1">
                Save
              </button>
              <button onClick={() => { setIsEditing(false); setEditText(report.finalReport); }}
                className="btn btn-ghost col-span-2 sm:col-span-1">
                Cancel
              </button>
            </>
          ) : (
            <>
              <button onClick={() => setIsEditing(true)} className="btn btn-ghost">
                Edit
              </button>
              <button onClick={approve} className="btn btn-accent" disabled={report.status === 'APPROVED'}>
                {report.status === 'APPROVED' ? <><CheckIcon size={16} /> Approved</> : 'Approve'}
              </button>
            </>
          )}
          <button onClick={copyToClipboard} className="btn btn-ghost">
            <CopyIcon size={16} /> Copy
          </button>
          <button onClick={() => downloadReportText(id).catch(() => setMessage('Download failed'))}
            className="btn btn-primary">
            <DownloadIcon size={16} /> Download
          </button>
        </div>

        {message && (
          <p className="flex items-center gap-1.5 font-semibold text-green-700 text-sm">
            <CheckIcon size={16} /> {message}
          </p>
        )}
      </section>
    </main>
  );
}
