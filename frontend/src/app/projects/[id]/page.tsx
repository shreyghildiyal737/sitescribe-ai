'use client';

import Link from 'next/link';
import { useParams } from 'next/navigation';
import { useEffect, useState } from 'react';
import { api, type Report } from '@/lib/api';
import { ReportCard } from '@/components/ReportCard';
import { ArrowLeftIcon, MicIcon, UsersIcon, ChevronRightIcon } from '@/components/icons';

type Member = {
  userId: string;
  name: string;
  email: string;
  roleOnProject: string;
};

export default function ProjectHub() {
  const { id } = useParams<{ id: string }>();
  const [members, setMembers] = useState<Member[]>([]);
  const [reports, setReports] = useState<Report[]>([]);

  useEffect(() => {
    api.getMembers(id)
      .then(data => setMembers(data as Member[]))
      .catch(() => setMembers([]));
    api.reportsByProject(id)
      .then(data => setReports(data as Report[]))
      .catch(() => setReports([]));
  }, [id]);

  const recentReports = reports.slice(0, 5);

  return (
    <main className="min-h-screen p-4">
      <section className="max-w-2xl mx-auto space-y-4">
        <Link href="/dashboard" className="link-muted">
          <ArrowLeftIcon size={16} /> Jobs
        </Link>

        {/* Primary action */}
        <Link href={`/projects/${id}/record`}
          className="btn btn-accent w-full min-h-16 text-lg">
          <MicIcon size={22} /> New Site Recording
        </Link>

        {/* Reports */}
        <div className="space-y-2">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black tracking-tight">Reports</h2>
            {reports.length > 5 && (
              <Link href={`/projects/${id}/reports`} className="link-muted text-amber-700 hover:text-amber-800">
                View all {reports.length} <ChevronRightIcon size={14} />
              </Link>
            )}
          </div>

          {recentReports.length === 0 ? (
            <div className="card p-4">
              <p className="text-stone-600 text-sm">No reports yet — tap the button above to start.</p>
            </div>
          ) : (
            recentReports.map(report => <ReportCard key={report.id} report={report} />)
          )}
        </div>

        {/* Team */}
        <div className="card p-5 space-y-3">
          <div className="flex items-center justify-between">
            <h2 className="text-lg font-black tracking-tight flex items-center gap-2">
              <UsersIcon size={18} /> Team
            </h2>
            <Link href={`/projects/${id}/team`} className="link-muted text-amber-700 hover:text-amber-800">
              Manage <ChevronRightIcon size={14} />
            </Link>
          </div>
          {members.length === 0 ? (
            <p className="text-stone-600 text-sm">No team members yet.</p>
          ) : (
            members.map(member => (
              <div key={member.userId}
                className="flex items-center justify-between p-3 rounded-2xl bg-stone-50 border border-stone-100">
                <div>
                  <p className="font-semibold">{member.name}</p>
                  <p className="text-sm text-stone-500">{member.email}</p>
                </div>
                <span className="badge bg-amber-100 text-amber-800">{member.roleOnProject}</span>
              </div>
            ))
          )}
        </div>
      </section>
    </main>
  );
}
