'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { api, type Report } from '@/lib/api';
import { ReportCard } from '@/components/ReportCard';
import { ArrowLeftIcon } from '@/components/icons';

export default function ReportsPage() {
  const { id } = useParams<{ id: string }>();
  const [reports, setReports] = useState<Report[]>([]);

  useEffect(() => {
    api.reportsByProject(id)
      .then(data => setReports(data as Report[]))
      .catch(() => setReports([]));
  }, [id]);

  return (
    <main className="min-h-screen p-4">
      <section className="max-w-2xl mx-auto space-y-3">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-black tracking-tight">All Reports</h1>
          <Link href={`/projects/${id}`} className="link-muted">
            <ArrowLeftIcon size={16} /> Back to Job
          </Link>
        </div>

        {reports.length === 0 ? (
          <div className="card p-5">
            <p className="text-stone-600">No reports yet. Record a site update to generate one.</p>
          </div>
        ) : (
          reports.map(report => <ReportCard key={report.id} report={report} />)
        )}
      </section>
    </main>
  );
}
