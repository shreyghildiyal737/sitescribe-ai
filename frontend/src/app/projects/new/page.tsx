'use client';

import { FormEvent, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';

export default function NewProject() {
  const router = useRouter();
  const [name, setName] = useState('');
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');

  async function submit(e: FormEvent) {
    e.preventDefault();
    try {
      await api.createProject({ name, address });
      router.push('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not create project');
    }
  }

  return (
    <main className="min-h-screen p-4 flex items-center justify-center">
      <form onSubmit={submit} className="card p-6 w-full max-w-md space-y-4">
        <h1 className="text-2xl font-black tracking-tight">New Job</h1>

        <input
          className="input"
          placeholder="Job name"
          value={name}
          onChange={e => setName(e.target.value)}
          required
        />

        <textarea
          className="input min-h-24"
          placeholder="Site address (optional)"
          value={address}
          onChange={e => setAddress(e.target.value)}
        />

        {error && (
          <p className="rounded-xl bg-red-50 border border-red-200 text-red-700 font-semibold text-sm p-3">{error}</p>
        )}

        <button className="btn btn-accent w-full">Create Job</button>
      </form>
    </main>
  );
}
