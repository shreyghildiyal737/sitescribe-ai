'use client';

import Link from 'next/link';
import { useParams, useRouter } from 'next/navigation';
import { FormEvent, useEffect, useRef, useState } from 'react';
import { api, uploadAudio } from '@/lib/api';
import { MicIcon, StopIcon, ArrowLeftIcon, SpinnerIcon } from '@/components/icons';

type RecordingState = 'idle' | 'recording' | 'recorded';

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60).toString().padStart(2, '0');
  const s = (seconds % 60).toString().padStart(2, '0');
  return `${m}:${s}`;
}

function createSilentWavBlob(): Blob {
  // Minimal valid WAV file — used when submitting manual transcript only
  const buffer = new ArrayBuffer(44);
  const view = new DataView(buffer);
  view.setUint32(0, 0x52494646, false);  // "RIFF"
  view.setUint32(4, 36, true);
  view.setUint32(8, 0x57415645, false);  // "WAVE"
  view.setUint32(12, 0x666d7420, false); // "fmt "
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);           // PCM
  view.setUint16(22, 1, true);           // mono
  view.setUint32(24, 16000, true);       // 16kHz
  view.setUint32(28, 32000, true);
  view.setUint16(32, 2, true);
  view.setUint16(34, 16, true);
  view.setUint32(36, 0x64617461, false); // "data"
  view.setUint32(40, 0, true);
  return new Blob([buffer], { type: 'audio/wav' });
}

export default function RecordPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();

  const [recordingState, setRecordingState] = useState<RecordingState>('idle');
  const [audioBlob, setAudioBlob] = useState<Blob | null>(null);
  const [audioUrl, setAudioUrl] = useState('');
  const [elapsed, setElapsed] = useState(0);
  const [outputLanguage, setOutputLanguage] = useState('auto');
  const [includeLocation, setIncludeLocation] = useState(true);
  const [manualTranscript, setManualTranscript] = useState('');
  const [status, setStatus] = useState('');
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');

  const mediaRecorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const timerRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const progressTimerRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    return () => {
      timerRef.current && clearInterval(timerRef.current);
      progressTimerRef.current && clearInterval(progressTimerRef.current);
      audioUrl && URL.revokeObjectURL(audioUrl);
    };
  }, [audioUrl]);

  async function startRecording() {
    setError('');
    try {
      const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      const recorder = new MediaRecorder(stream);
      mediaRecorderRef.current = recorder;
      chunksRef.current = [];

      recorder.ondataavailable = e => {
        if (e.data.size > 0) chunksRef.current.push(e.data);
      };

      recorder.onstop = () => {
        const blob = new Blob(chunksRef.current, { type: recorder.mimeType });
        const url = URL.createObjectURL(blob);
        setAudioBlob(blob);
        setAudioUrl(url);
        setRecordingState('recorded');
        stream.getTracks().forEach(t => t.stop());
      };

      recorder.start();
      setRecordingState('recording');
      setElapsed(0);
      timerRef.current = setInterval(() => setElapsed(s => s + 1), 1000);
    } catch {
      setError('Microphone access denied. Please allow microphone access and try again.');
    }
  }

  function stopRecording() {
    mediaRecorderRef.current?.stop();
    timerRef.current && clearInterval(timerRef.current);
  }

  function resetRecording() {
    setAudioBlob(null);
    setAudioUrl('');
    setRecordingState('idle');
    setElapsed(0);
    setError('');
  }

  async function getLocation(): Promise<GeolocationCoordinates | undefined> {
    return new Promise(resolve => {
      if (!navigator.geolocation) return resolve(undefined);
      navigator.geolocation.getCurrentPosition(
        p => resolve(p.coords),
        () => resolve(undefined),
        { timeout: 5000 }
      );
    });
  }

  function startProgress() {
    setProgress(0);
    let p = 0;
    progressTimerRef.current = setInterval(() => {
      p = Math.min(p + 2, 80);
      setProgress(p);
    }, 300);
  }

  function finishProgress() {
    progressTimerRef.current && clearInterval(progressTimerRef.current);
    setProgress(100);
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (recordingState === 'idle' && !manualTranscript.trim()) {
      setError('Record audio or paste a transcript first.');
      return;
    }

    try {
      setStatus('Uploading audio...');
      startProgress();

      const coords = includeLocation ? await getLocation() : undefined;

      // Use recorded audio, or a silent WAV placeholder for transcript-only submissions
      const mimeType = audioBlob?.type ?? 'audio/wav';
      const ext = mimeType.includes('mp4') ? 'mp4' : mimeType.includes('ogg') ? 'ogg' : 'webm';
      const file = audioBlob
        ? new File([audioBlob], `recording.${ext}`, { type: mimeType })
        : new File([createSilentWavBlob()], 'transcript-only.wav', { type: 'audio/wav' });

      const upload = await uploadAudio(id, file, coords) as { id: string };

      setStatus('Processing audio and generating report (up to 20 seconds)...');
      const report = await api.createReportFromUpload({
        uploadId: upload.id,
        manualTranscript,
        outputLanguage,
      }) as { id: string };

      finishProgress();
      router.push(`/reports/${report.id}`);
    } catch (err) {
      finishProgress();
      setError(err instanceof Error ? err.message : 'Failed');
      setStatus('');
    }
  }

  const isProcessing = status !== '' && !error;

  return (
    <main className="min-h-screen p-4">
      <form onSubmit={submit} className="card p-5 max-w-2xl mx-auto space-y-5">
        <div>
          <Link href={`/projects/${id}`} className="link-muted">
            <ArrowLeftIcon size={16} /> Back
          </Link>
          <h1 className="text-2xl font-black tracking-tight mt-1">New Site Update</h1>
        </div>

        {/* Data processing notice — Article 13 GDPR */}
        <div className="rounded-xl bg-stone-50 border border-stone-200 p-3 text-xs text-gray-600 space-y-1">
          <p className="font-semibold text-gray-800">How your data is used</p>
          <p>Your voice recording is sent to Groq (transcription) and Google Gemini (report generation), then <strong>deleted from our servers</strong>. Only the text transcript and report are kept. Location is optional and used only to tag this report.</p>
          <p>Data is processed on behalf of your employer under a legitimate business interest. See our <Link href="/privacy" className="underline text-amber-700">Privacy Notice</Link> for your rights under GDPR.</p>
        </div>

        {/* Recording controls */}
        <div className="flex flex-col items-center gap-4 py-2">
          {recordingState === 'idle' && (
            <button type="button" onClick={startRecording}
              className="w-28 h-28 rounded-full bg-amber-500 hover:bg-amber-400 active:scale-95 transition-all shadow-lg shadow-amber-500/30 flex flex-col items-center justify-center text-stone-900 gap-1.5 focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-amber-500/40">
              <MicIcon size={36} />
              <span className="text-xs font-bold">Tap to record</span>
            </button>
          )}

          {recordingState === 'recording' && (
            <div className="flex flex-col items-center gap-2">
              <button type="button" onClick={stopRecording}
                className="w-28 h-28 rounded-full bg-red-600 hover:bg-red-500 active:scale-95 transition-all shadow-lg shadow-red-600/30 flex flex-col items-center justify-center text-white gap-1.5 animate-pulse">
                <StopIcon size={36} />
                <span className="text-xs font-bold">Tap to stop</span>
              </button>
              <p className="font-mono text-2xl font-bold text-red-600 tabular-nums">{formatTime(elapsed)}</p>
            </div>
          )}

          {recordingState === 'recorded' && (
            <div className="w-full space-y-3">
              <p className="text-center text-sm font-semibold text-green-700">
                Recording complete — {formatTime(elapsed)}
              </p>
              <audio controls src={audioUrl} className="w-full rounded-xl" />
              <button type="button" onClick={resetRecording} className="btn btn-ghost w-full">
                Re-record
              </button>
            </div>
          )}
        </div>

        {/* Language selector */}
        <div className="space-y-2">
          <p className="font-semibold text-sm">Report language</p>
          <div className="grid grid-cols-3 gap-2">
            {[
              { value: 'auto', label: 'Auto-detect' },
              { value: 'en', label: 'English' },
              { value: 'ga', label: 'Gaeilge' },
              { value: 'es', label: 'Español' },
              { value: 'uk', label: 'Українська' },
              { value: 'bilingual', label: 'EN + GA' },
            ].map(opt => (
              <label key={opt.value}
                className={`tile min-h-14 ${outputLanguage === opt.value ? 'tile-active' : ''}`}>
                <input type="radio" name="outputLanguage" value={opt.value} className="sr-only"
                  checked={outputLanguage === opt.value}
                  onChange={() => setOutputLanguage(opt.value)} />
                {opt.label}
              </label>
            ))}
          </div>
          {outputLanguage === 'auto' && (
            <p className="text-xs text-gray-500">Language will be detected automatically from your recording.</p>
          )}
        </div>

        {/* Optional manual transcript */}
        <textarea
          className="input min-h-24 text-sm"
          placeholder="Optional: paste a transcript instead of recording audio."
          value={manualTranscript}
          onChange={e => setManualTranscript(e.target.value)}
        />

        {/* Location opt-in */}
        <label className="flex items-start gap-3 cursor-pointer">
          <input
            type="checkbox"
            className="mt-1 w-4 h-4 accent-amber-500"
            checked={includeLocation}
            onChange={e => setIncludeLocation(e.target.checked)}
          />
          <span className="text-sm text-gray-700">
            <span className="font-semibold">Include GPS location</span> — tags this report with your current site coordinates (optional, recommended).
          </span>
        </label>

        {/* Progress indicator */}
        {isProcessing && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 font-bold text-amber-800">
              <SpinnerIcon size={16} className="text-amber-600" />
              {status}
            </div>
            <div className="w-full bg-stone-200 rounded-full h-2 overflow-hidden">
              <div className="bg-amber-500 h-2 rounded-full transition-all duration-300"
                style={{ width: `${progress}%` }} />
            </div>
          </div>
        )}

        {error && (
          <p className="rounded-xl bg-red-50 border border-red-200 text-red-700 font-semibold text-sm p-3">
            {error}
          </p>
        )}

        <button
          className="btn btn-accent w-full min-h-14 text-lg"
          disabled={isProcessing || recordingState === 'recording'}>
          {isProcessing ? 'Processing…' : 'Generate Report'}
        </button>
      </form>
    </main>
  );
}
