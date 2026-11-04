import { getSession, clearSession } from './session';

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? '/api';

export type ApiResponse<T> = { success: boolean; data: T; message?: string };

export type Report = {
  id: string;
  status: string;
  finalReport: string;
  lowConfidence: boolean;
  outputLanguage: string;
  createdAt: string;
};

function authHeaders(): Record<string, string> {
  const session = getSession();
  return session ? { Authorization: `Bearer ${session.token}` } : {};
}

function handleExpiredSession() {
  // An expired/invalid token (e.g. after a DB reset) returns 401/403 with an
  // empty body. Clear the dead session and send the user to sign in again.
  if (typeof window === 'undefined') return;
  if (getSession()) clearSession();
  if (window.location.pathname !== '/login') window.location.href = '/login';
}

async function parseJson<T>(res: Response): Promise<ApiResponse<T> | null> {
  // Tolerate empty / non-JSON bodies instead of throwing "Unexpected end of JSON input".
  const text = await res.text();
  if (!text) return null;
  try {
    return JSON.parse(text) as ApiResponse<T>;
  } catch {
    return null;
  }
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...authHeaders(),
      ...(options?.headers ?? {}),
    },
  });

  if (res.status === 401 || res.status === 403) {
    handleExpiredSession();
    throw new Error('Your session has expired. Please sign in again.');
  }

  const json = await parseJson<T>(res);
  if (!res.ok || !json || !json.success) {
    throw new Error(json?.message ?? `Request failed (${res.status})`);
  }
  return json.data;
}

export const api = {
  bootstrap: (body: { companyName: string; name: string; email: string; preferredLanguage?: string; password: string }) =>
    request('/auth/bootstrap', { method: 'POST', body: JSON.stringify(body) }),

  login: (email: string, password: string) =>
    request('/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) }),

  createProject: (body: { name: string; address: string }) =>
    request('/projects', { method: 'POST', body: JSON.stringify(body) }),

  myProjects: () =>
    request('/projects/my'),

  getProject: (id: string) =>
    request(`/projects/${id}`),

  getMembers: (projectId: string) =>
    request(`/projects/${projectId}/members`),

  inviteMember: (projectId: string, body: { name: string; email: string; role: string; roleOnProject: string; preferredLanguage: string }) =>
    request(`/projects/${projectId}/assignments`, { method: 'POST', body: JSON.stringify(body) }),

  reportsByProject: (projectId: string) =>
    request(`/reports/project/${projectId}`),

  createReportFromUpload: (body: { uploadId: string; manualTranscript: string; outputLanguage: string | null }) =>
    request('/reports/from-upload', { method: 'POST', body: JSON.stringify(body) }),

  getReport: (id: string) =>
    request(`/reports/${id}`),

  updateReport: (id: string, finalReport: string) =>
    request(`/reports/${id}`, { method: 'PATCH', body: JSON.stringify({ finalReport }) }),

  approveReport: (id: string) =>
    request(`/reports/${id}/approve`, { method: 'POST' }),
};

// Exports now require auth, so we can't use a plain <a href>. Fetch with the
// Bearer header, then trigger a client-side download from the returned blob.
export async function downloadReportText(id: string) {
  const res = await fetch(`${API_BASE}/exports/reports/${id}/text`, { headers: authHeaders() });
  if (!res.ok) throw new Error('Download failed');
  const blob = await res.blob();
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `site-report-${id}.txt`;
  document.body.appendChild(a);
  a.click();
  a.remove();
  URL.revokeObjectURL(url);
}

export async function uploadAudio(
  projectId: string,
  file: File,
  coords?: GeolocationCoordinates
) {
  const form = new FormData();
  form.append('file', file);
  if (coords) {
    form.append('latitude', String(coords.latitude));
    form.append('longitude', String(coords.longitude));
    form.append('accuracyMeters', String(coords.accuracy));
  }
  const res = await fetch(`${API_BASE}/projects/${projectId}/uploads/audio`, {
    method: 'POST',
    headers: authHeaders(),
    body: form,
  });
  if (res.status === 401 || res.status === 403) {
    handleExpiredSession();
    throw new Error('Your session has expired. Please sign in again.');
  }
  const json = await parseJson<unknown>(res) as { success?: boolean; data?: unknown; message?: string } | null;
  if (!res.ok || !json || !json.success) throw new Error(json?.message ?? `Upload failed (${res.status})`);
  return json.data;
}
