import { withSentryConfig } from '@sentry/nextjs';

/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8090/api/:path*',
      },
    ];
  },
};

export default withSentryConfig(nextConfig, {
  silent: true,
  // Source map upload disabled — set SENTRY_AUTH_TOKEN + org/project to enable
  sourcemaps: { disable: true },
});
