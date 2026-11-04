'use client';

import Link from 'next/link';
import { FormEvent, useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { api } from '@/lib/api';
import { saveSession } from '@/lib/session';

type Mode = 'bootstrap' | 'login';

export default function LoginPage() {
  const router = useRouter();
  const [mode, setMode] = useState<Mode>('bootstrap');

  // Open the Sign In tab when arriving from the landing page's "Sign in" link.
  useEffect(() => {
    if (new URLSearchParams(window.location.search).get('mode') === 'login') setMode('login');
  }, []);
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [companyName, setCompanyName] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      const data = mode === 'bootstrap'
        ? await api.bootstrap({ companyName, name, email, preferredLanguage: 'en', password })
        : await api.login(email, password);
      saveSession(data);
      router.push('/dashboard');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not continue');
    }
  }

  return (
    <main className="min-h-screen p-4 flex items-center justify-center">
      <form onSubmit={submit} className="card p-6 w-full max-w-md space-y-4">
        <h1 className="text-2xl font-black tracking-tight">SiteScribe AI</h1>

        <div className="grid grid-cols-2 gap-2 p-1 bg-stone-100 rounded-2xl">
          <button type="button" onClick={() => setMode('bootstrap')}
            className={`py-2.5 rounded-xl font-semibold text-sm transition-colors ${mode === 'bootstrap' ? 'bg-white text-stone-900 shadow-sm' : 'text-stone-500'}`}>
            Register
          </button>
          <button type="button" onClick={() => setMode('login')}
            className={`py-2.5 rounded-xl font-semibold text-sm transition-colors ${mode === 'login' ? 'bg-white text-stone-900 shadow-sm' : 'text-stone-500'}`}>
            Sign In
          </button>
        </div>

        {mode === 'bootstrap' ? (
          <>
            <p className="text-sm text-stone-500">Set up your company account. Workers can be added later.</p>
            <input className="input" placeholder="Company name"
              value={companyName} onChange={e => setCompanyName(e.target.value)} required />
            <input className="input" placeholder="Your name"
              value={name} onChange={e => setName(e.target.value)} required />
          </>
        ) : (
          <p className="text-sm text-stone-500">Enter your work email and password to sign in.</p>
        )}

        <input className="input" type="email" placeholder="Email"
          value={email} onChange={e => setEmail(e.target.value)} required />

        <input className="input" type="password" placeholder="Password"
          value={password} onChange={e => setPassword(e.target.value)} required minLength={8} />

        {error && (
          <p className="rounded-xl bg-red-50 border border-red-200 text-red-700 font-semibold text-sm p-3">{error}</p>
        )}

        <button className="btn btn-accent w-full">
          {mode === 'bootstrap' ? 'Create Account' : 'Sign In'}
        </button>

        <p className="text-xs text-stone-400 text-center">
          By continuing you agree to our{' '}
          <Link href="/privacy" className="underline text-amber-700">Privacy Notice</Link>.
          Your data is processed to provide site reporting services. Voice recordings are deleted after transcription.
        </p>
      </form>
    </main>
  );
}
