import './globals.css';
import type { Metadata, Viewport } from 'next';

export const metadata: Metadata = {
  title: 'SiteScribe AI — Voice notes into clean site reports',
  description:
    'Construction site updates by voice. AI drafts the report, your manager reviews and approves. Built for Irish site teams, with Gaeilge and multilingual support.',
};

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#1c1917',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  );
}
