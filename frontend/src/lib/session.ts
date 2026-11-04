export type Session = { userId: string; companyId: string; name: string; email: string; role: string; token: string };
const KEY='sitescribe-session';
export function saveSession(s: unknown){ if(typeof window !== 'undefined') localStorage.setItem(KEY, JSON.stringify(s)); }
export function getSession(): Session | null { if(typeof window === 'undefined') return null; const raw=localStorage.getItem(KEY); return raw ? JSON.parse(raw) as Session : null; }
export function clearSession(){ if(typeof window !== 'undefined') localStorage.removeItem(KEY); }
