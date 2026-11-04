import { STATUS_STYLES, STATUS_LABELS } from '@/lib/format';
import { AlertIcon } from './icons';

export function StatusBadge({ status }: { status: string }) {
  return (
    <span className={`badge ${STATUS_STYLES[status] ?? 'bg-stone-100 text-stone-700'}`}>
      {STATUS_LABELS[status] ?? status}
    </span>
  );
}

export function ReviewBadge() {
  return (
    <span className="badge bg-orange-100 text-orange-800">
      <AlertIcon size={12} /> Review needed
    </span>
  );
}
