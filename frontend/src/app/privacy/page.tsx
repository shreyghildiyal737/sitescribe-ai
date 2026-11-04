import Link from 'next/link';

export default function PrivacyPage() {
  return (
    <main className="min-h-screen p-4">
      <article className="max-w-2xl mx-auto space-y-6 py-6">
        <div>
          <Link href="/dashboard" className="text-amber-700 font-semibold text-sm">← Back</Link>
          <h1 className="text-2xl font-black mt-2">Privacy Notice</h1>
          <p className="text-sm text-gray-500 mt-1">Last updated: May 2025 — SiteScribe AI</p>
        </div>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">1. Who processes your data</h2>
          <p className="text-sm text-gray-700">
            SiteScribe AI acts as a data processor on behalf of your employer (the data controller), who operates the platform for construction site reporting. Your employer is responsible for the lawful basis of processing under GDPR Article 6.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">2. What data we collect</h2>
          <ul className="text-sm text-gray-700 list-disc pl-5 space-y-1">
            <li><strong>Account data</strong>: your name and email address, used to identify you within your company's workspace.</li>
            <li><strong>Voice recordings</strong>: audio captured when you use the record feature. Sent to Groq Inc. for transcription, then <strong>permanently deleted from our servers</strong> once the transcript is generated.</li>
            <li><strong>Transcripts and reports</strong>: text derived from your recordings, stored as the work product of this service.</li>
            <li><strong>Location (optional)</strong>: GPS coordinates if you opt in. Used only to tag the relevant site report.</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">3. Why we process it (lawful basis)</h2>
          <p className="text-sm text-gray-700">
            Processing is necessary for the performance of the service contract between your employer and SiteScribe AI (Article 6(1)(b)). Site reporting is part of your employment duties; your employer holds the legitimate business interest basis where applicable.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">4. AI sub-processors (third parties)</h2>
          <p className="text-sm text-gray-700">
            We use the following sub-processors to deliver the service. Each is bound by a Data Processing Agreement:
          </p>
          <ul className="text-sm text-gray-700 list-disc pl-5 space-y-1">
            <li><strong>Groq Inc. (USA)</strong> — audio transcription via Whisper model. Audio is sent over TLS and not retained by Groq after the API response. Transfer is covered by EU Standard Contractual Clauses (SCCs).</li>
            <li><strong>Google LLC (USA)</strong> — report generation via Gemini 2.0 Flash. Text transcript is sent over TLS. Google's EU data processing addendum applies. Transfer is covered by SCCs.</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">5. How long we keep data</h2>
          <ul className="text-sm text-gray-700 list-disc pl-5 space-y-1">
            <li><strong>Voice recordings</strong>: deleted immediately after transcription is complete.</li>
            <li><strong>Transcripts and reports</strong>: retained for the life of the project plus 7 years, in line with Irish construction record-keeping requirements under the Safety, Health and Welfare at Work (Construction) Regulations 2013.</li>
            <li><strong>Account data</strong>: retained while your account is active and for 12 months after closure.</li>
            <li><strong>Location data</strong>: retained as part of the associated report (same schedule as reports).</li>
          </ul>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">6. Your rights under GDPR</h2>
          <p className="text-sm text-gray-700">You have the right to:</p>
          <ul className="text-sm text-gray-700 list-disc pl-5 space-y-1">
            <li><strong>Access</strong>: request a copy of your personal data we hold.</li>
            <li><strong>Rectification</strong>: correct inaccurate data.</li>
            <li><strong>Erasure</strong>: request deletion of your data where there is no overriding legal basis to retain it.</li>
            <li><strong>Restriction</strong>: ask us to pause processing while a dispute is resolved.</li>
            <li><strong>Portability</strong>: receive your data in a structured, machine-readable format.</li>
            <li><strong>Objection</strong>: object to processing based on legitimate interests.</li>
          </ul>
          <p className="text-sm text-gray-700 mt-2">
            To exercise any right, contact your employer's designated data contact, or email <strong>privacy@sitescribe.ai</strong>. We will respond within 30 days.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">7. Right to lodge a complaint</h2>
          <p className="text-sm text-gray-700">
            If you are based in Ireland or the EU, you have the right to lodge a complaint with the <strong>Data Protection Commission (DPC)</strong> at{' '}
            <a href="https://www.dataprotection.ie" className="underline text-amber-700" target="_blank" rel="noopener noreferrer">
              dataprotection.ie
            </a>. If based elsewhere in the EU, you may contact your national supervisory authority.
          </p>
        </section>

        <section className="space-y-2">
          <h2 className="text-lg font-bold">8. Security</h2>
          <p className="text-sm text-gray-700">
            All data is transmitted over TLS. Audio files are stored temporarily in an isolated directory with restricted access and are deleted immediately after transcription. Reports are stored in a private database accessible only to authorised users within your company's workspace.
          </p>
        </section>

        <p className="text-xs text-gray-400 pt-4 border-t">
          This notice applies to SiteScribe AI. It does not cover your employer's broader data practices. Contact your employer for their full privacy policy.
        </p>
      </article>
    </main>
  );
}
