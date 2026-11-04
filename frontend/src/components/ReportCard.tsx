import Link from 'next/link';
import type { Report } from '@/lib/api';
import { formatDate } from '@/lib/format';
import { StatusBadge, ReviewBadge } from './StatusBadge';

export function ReportCard({ report }: { report: Report }) {
  return (
    <Link
      href={`/reports/${report.id}`}
      className="card p-4 block space-y-2 hover:border-stone-300 transition-colors"
    >
      <div className="flex items-center justify-between gap-2 flex-wrap">
        <div className="flex items-center gap-2">
          <StatusBadge status={report.status} />
          {report.lowConfidence && <ReviewBadge />}
        </div>
        <span className="text-xs text-stone-500">{formatDate(report.createdAt)}</span>
      </div>
      <p className="text-stone-700 text-sm line-clamp-2">{report.finalReport}</p>
    </Link>
  );
}
