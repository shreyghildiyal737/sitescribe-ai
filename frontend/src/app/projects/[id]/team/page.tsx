'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { api } from '@/lib/api';
import { ArrowLeftIcon } from '@/components/icons';

type Member = {
  userId: string;
  name: string;
  email: string;
  roleOnProject: string;
};

const ROLES = ['WORKER', 'MANAGER', 'OWNER'];

export default function TeamPage() {
  const { id } = useParams<{ id: string }>();
  const [members, setMembers] = useState<Member[]>([]);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [role, setRole] = useState('WORKER');
  const [roleOnProject, setRoleOnProject] = useState('Site Worker');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    api.getMembers(id)
      .then(data => setMembers(data as Member[]))
      .catch(() => setMembers([]));
  }, [id]);

  async function invite(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    try {
      await api.inviteMember(id, { name, email, role, roleOnProject, preferredLanguage: 'en' });
      setSuccess(`${name} has been added to this job.`);
      setName('');
      setEmail('');
      setRoleOnProject('Site Worker');
      const updated = await api.getMembers(id);
      setMembers(updated as Member[]);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not add worker');
    }
  }

  return (
    <main className="min-h-screen p-4">
      <section className="max-w-2xl mx-auto space-y-4">
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-black tracking-tight">Team</h1>
          <Link href={`/projects/${id}`} className="link-muted">
            <ArrowLeftIcon size={16} /> Back to Job
          </Link>
        </div>

        {/* Add worker form */}
        <div className="card p-5 space-y-4">
          <h2 className="text-lg font-bold">Add a Worker</h2>
          <form onSubmit={invite} className="space-y-3">
            <input
              className="input"
              placeholder="Full name"
              value={name}
              onChange={e => setName(e.target.value)}
              required
            />
            <input
              className="input"
              type="email"
              placeholder="Work email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
            />
            <input
              className="input"
              placeholder="Role on this job (e.g. Plasterer, Foreman)"
              value={roleOnProject}
              onChange={e => setRoleOnProject(e.target.value)}
              required
            />
            <div className="flex gap-2">
              {ROLES.map(r => (
                <label key={r} className={`tile flex-1 min-h-12 ${role === r ? 'tile-active' : ''}`}>
                  <input type="radio" name="role" value={r} className="sr-only"
                    checked={role === r} onChange={() => setRole(r)} />
                  {r.charAt(0) + r.slice(1).toLowerCase()}
                </label>
              ))}
            </div>
            {error && (
              <p className="rounded-xl bg-red-50 border border-red-200 text-red-700 font-semibold text-sm p-3">{error}</p>
            )}
            {success && (
              <p className="rounded-xl bg-green-50 border border-green-200 text-green-700 font-semibold text-sm p-3">{success}</p>
            )}
            <button className="btn btn-accent w-full">Add to Job</button>
          </form>
        </div>

        {/* Current team */}
        <div className="card p-5 space-y-3">
          <h2 className="text-lg font-bold">Current Team ({members.length})</h2>
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
