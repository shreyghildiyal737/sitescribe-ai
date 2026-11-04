// Shared formatting + report status presentation (deduped from the page components).

export function formatDate(iso: string, withYear = false): string {
  return new Date(iso).toLocaleString('en-IE', {
    day: 'numeric',
    month: 'short',
    ...(withYear ? { year: 'numeric' } : {}),
    hour: '2-digit',
    minute: '2-digit',
  });
}

export const STATUS_STYLES: Record<string, string> = {
  DRAFT: 'bg-amber-100 text-amber-800',
  NEEDS_REVIEW: 'bg-red-100 text-red-800',
  APPROVED: 'bg-green-100 text-green-800',
  EXPORTED: 'bg-stone-100 text-stone-700',
};

export const STATUS_LABELS: Record<string, string> = {
  DRAFT: 'Draft',
  NEEDS_REVIEW: 'Needs Review',
  APPROVED: 'Approved',
  EXPORTED: 'Exported',
};
