import Link from 'next/link';
import { MicIcon, FileTextIcon, CheckIcon, MapPinIcon, ChevronRightIcon } from '@/components/icons';

const STEPS = [
  { icon: MicIcon, title: 'Record on site', body: 'A worker taps record and speaks the update — in English, Gaeilge, or their own language.' },
  { icon: FileTextIcon, title: 'AI drafts the report', body: 'Speech becomes a clean, structured site report in seconds. No typing on site.' },
  { icon: CheckIcon, title: 'Manager approves', body: 'A manager reviews, edits anything, approves, and exports an export-ready report.' },
];

export default function Home() {
  return (
    <main className="min-h-screen">
      {/* Hero */}
      <section className="hero-mesh">
        <div className="max-w-5xl mx-auto px-6 pt-16 pb-20 sm:pt-24 sm:pb-28">
          <p className="inline-flex items-center gap-2 text-sm font-bold text-amber-700 bg-amber-100/70 border border-amber-200 rounded-full px-3 py-1">
            <MicIcon size={14} /> SiteScribe AI
          </p>
          <h1 className="mt-5 text-4xl sm:text-6xl font-black tracking-tight leading-[1.05] max-w-3xl">
            Site updates into clean reports.
          </h1>
          <p className="mt-5 text-lg sm:text-xl text-stone-600 max-w-xl leading-relaxed">
            Workers speak. AI writes the daily site report. Managers review and approve.
            Built for Irish construction teams — with Gaeilge and multilingual support.
          </p>
          <div className="mt-8 flex flex-col sm:flex-row gap-3">
            <Link className="btn btn-accent text-base px-7 py-4" href="/login">
              Get started <ChevronRightIcon size={18} />
            </Link>
            <Link className="btn btn-ghost text-base px-7 py-4" href="/login?mode=login">
              Sign in
            </Link>
          </div>
          <p className="mt-6 flex items-center gap-2 text-sm text-stone-500">
            <MapPinIcon size={16} /> GDPR-ready · audio deleted after transcription · no worker tracking
          </p>
        </div>
      </section>

      {/* How it works */}
      <section className="max-w-5xl mx-auto px-6 py-16 sm:py-20">
        <h2 className="text-2xl sm:text-3xl font-black tracking-tight">From voice note to filed report in three steps</h2>
        <div className="mt-8 grid gap-4 sm:grid-cols-3">
          {STEPS.map((s, i) => (
            <div key={s.title} className="card p-6 space-y-3">
              <div className="flex items-center justify-center w-11 h-11 rounded-2xl bg-stone-900 text-white">
                <s.icon size={20} />
              </div>
              <p className="text-xs font-bold text-stone-400">STEP {i + 1}</p>
              <h3 className="text-lg font-bold">{s.title}</h3>
              <p className="text-sm text-stone-600 leading-relaxed">{s.body}</p>
            </div>
          ))}
        </div>

        <div className="mt-12 card p-7 sm:p-9 flex flex-col sm:flex-row sm:items-center justify-between gap-5">
          <div>
            <h3 className="text-xl font-black">Stop writing reports at 6pm.</h3>
            <p className="mt-1 text-stone-600">Set up your company in under a minute.</p>
          </div>
          <Link className="btn btn-primary text-base px-7 py-4 shrink-0" href="/login">
            Create your account <ChevronRightIcon size={18} />
          </Link>
        </div>
      </section>

      <footer className="max-w-5xl mx-auto px-6 pb-12 text-sm text-stone-400 flex flex-wrap gap-x-4 gap-y-1">
        <span>© {new Date().getFullYear()} SiteScribe AI</span>
        <Link href="/privacy" className="hover:text-stone-700">Privacy Notice</Link>
      </footer>
    </main>
  );
}
