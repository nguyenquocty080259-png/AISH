// Giữ nguyên hardcode localhost:8080 như code cũ (trap đã biết, sửa ở việc khác, KHÔNG đụng ở đây).
const GOOGLE_URL = "http://localhost:8080/oauth2/authorization/google";
const GITHUB_URL = "http://localhost:8080/oauth2/authorization/github";

export default function SocialButtons() {
  return (
    <div className="grid grid-cols-2 gap-3">
      <button type="button" onClick={() => { window.location.href = GOOGLE_URL; }}
        className="flex items-center justify-center gap-2 rounded-input border border-border bg-surface py-2.5 font-semibold text-app transition-colors hover:border-primary">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 11v2.9h4.05a3.5 3.5 0 0 1-1.5 2.3v1.9h2.43A7.4 7.4 0 0 0 19.5 12c0-.5-.05-1-.13-1.45H12Z"/><path d="M12 19.5c1.98 0 3.64-.65 4.85-1.77l-2.43-1.9c-.67.45-1.53.72-2.42.72-1.86 0-3.44-1.26-4-2.95H5.47v1.9A7.5 7.5 0 0 0 12 19.5Z"/><path d="M8 13.6a4.5 4.5 0 0 1 0-2.9v-1.9H5.47a7.5 7.5 0 0 0 0 6.7L8 13.6Z"/><path d="M12 8.45c1.05 0 2 .36 2.74 1.07l2.05-2.05A7.2 7.2 0 0 0 5.47 8.8L8 10.7c.56-1.69 2.14-2.95 4-2.95Z"/></svg>
        Google
      </button>
      <button type="button" onClick={() => { window.location.href = GITHUB_URL; }}
        className="flex items-center justify-center gap-2 rounded-input border border-border bg-surface py-2.5 font-semibold text-app transition-colors hover:border-primary">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 2A10 10 0 0 0 8.84 21.5c.5.08.66-.22.66-.48v-1.7c-2.77.6-3.36-1.34-3.36-1.34-.46-1.16-1.11-1.46-1.11-1.46-.9-.62.07-.6.07-.6 1 .07 1.53 1.03 1.53 1.03.9 1.52 2.34 1.08 2.91.83.09-.65.35-1.09.63-1.34-2.2-.25-4.52-1.1-4.52-4.9 0-1.08.39-1.97 1.03-2.66-.1-.26-.45-1.27.1-2.64 0 0 .84-.27 2.75 1.02a9.5 9.5 0 0 1 5 0c1.9-1.3 2.74-1.02 2.74-1.02.55 1.37.2 2.38.1 2.64.64.7 1.03 1.58 1.03 2.66 0 3.8-2.33 4.65-4.55 4.9.36.31.68.92.68 1.85v2.74c0 .27.16.57.67.48A10 10 0 0 0 12 2Z"/></svg>
        GitHub
      </button>
    </div>
  );
}
