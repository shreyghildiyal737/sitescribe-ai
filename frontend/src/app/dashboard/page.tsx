'use client';

import Link from 'next/link';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { getSession, clearSession } from '@/lib/session';
import { MicIcon, PlusIcon, LogOutIcon, HardHatIcon, MapPinIcon, UsersIcon } from '@/components/icons';

type ProjectSummary = {
  id: string;
  name: string;
  address?: string;
  roleOnProject: string;
  activeWorkers: number;
  pendingReports: number;
};

export default function Dashboard() {
  const [projects, setProjects] = useState<ProjectSummary[]>([]);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const session = getSession();

  function logout() {
    clearSession();
    router.push('/login');
  }

  useEffect(() => {
    if (!session) {
      router.replace('/login');
      return;
    }
    api.myProjects()
      .then(data => setProjects(data as ProjectSummary[]))
      .catch(() => setProjects([]))
      .finally(() => setLoading(false));
  }, []);

  return (
    <main className="min-h-screen p-4">
      <section className="max-w-3xl mx-auto space-y-4">
        <header className="flex items-center justify-between">
          <div>
            <p className="font-bold text-amber-700 text-sm">SiteScribe AI</p>
            <h1 className="text-2xl font-black tracking-tight">My Jobs</h1>
            {session && <p className="text-sm text-stone-500">{session.name}</p>}
          </div>
          <div className="flex gap-2">
            <Link className="btn btn-primary text-sm" href="/projects/new">
              <PlusIcon size={16} /> New Job
            </Link>
            <button onClick={logout} className="btn btn-ghost text-sm" aria-label="Sign out">
              <LogOutIcon size={16} />
            </button>
          </div>
        </header>

        {loading ? (
          <div className="space-y-4">
            {[0, 1].map(i => (
              <div key={i} className="card p-5 h-32 animate-pulse bg-stone-50" />
            ))}
          </div>
        ) : projects.length === 0 ? (
          <div className="card p-8 space-y-3 text-center">
            <div className="mx-auto flex items-center justify-center w-14 h-14 rounded-2xl bg-amber-100 text-amber-700">
              <HardHatIcon size={28} />
            </div>
            <p className="font-bold text-lg">No jobs yet</p>
            <p className="text-stone-600 text-sm">Create your first job to start recording site updates.</p>
            <Link className="btn btn-accent inline-flex" href="/projects/new">
              <PlusIcon size={16} /> Create a Job
            </Link>
          </div>
        ) : (
          projects.map(project => (
            <div key={project.id} className="card p-5 space-y-3">
              <Link href={`/projects/${project.id}`} className="block">
                <h2 className="text-xl font-black tracking-tight">{project.name}</h2>
                <p className="text-stone-500 text-sm flex items-center gap-1.5 mt-0.5">
                  <MapPinIcon size={14} /> {project.address || 'No address added'}
                </p>
                <div className="mt-3 flex gap-2 flex-wrap text-sm">
                  <span className="badge bg-amber-100 text-amber-800">{project.roleOnProject}</span>
                  {project.pendingReports > 0 && (
                    <span className="badge bg-red-100 text-red-800">{project.pendingReports} pending review</span>
                  )}
                  {project.activeWorkers > 0 && (
                    <span className="badge bg-stone-100 text-stone-700">
                      <UsersIcon size={12} /> {project.activeWorkers}
                    </span>
                  )}
                </div>
              </Link>
              <div className="flex gap-2 pt-3 border-t border-stone-100">
                <Link href={`/projects/${project.id}/record`} className="btn btn-accent flex-1 text-sm">
                  <MicIcon size={16} /> Record
                </Link>
                <Link href={`/projects/${project.id}`} className="btn btn-ghost flex-1 text-sm">
                  View Job
                </Link>
              </div>
            </div>
          ))
        )}
      </section>
    </main>
  );
}
