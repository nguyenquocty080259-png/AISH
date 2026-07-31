import { useTranslation } from "react-i18next";

// Bộ chuyển ngôn ngữ VI/EN. Đổi ngôn ngữ áp dụng ngay (react-i18next re-render),
// không reload trang. Lựa chọn được i18next-browser-languagedetector lưu vào
// localStorage (key i18nextLng) nên lần vào sau giữ nguyên.
export default function LanguageSwitcher({ className = "" }) {
  const { i18n } = useTranslation();
  const current = i18n.resolvedLanguage || i18n.language || "vi";

  const setLang = (lng) => {
    if (lng !== current) i18n.changeLanguage(lng);
  };

  const langs = [
    { code: "vi", label: "VI" },
    { code: "en", label: "EN" },
  ];

  return (
    <div
      className={`inline-flex items-center rounded-pill border border-border bg-surface-2 p-0.5 ${className}`}
      role="group"
      aria-label="Language"
    >
      {langs.map((l) => {
        const active = current === l.code;
        return (
          <button
            key={l.code}
            type="button"
            onClick={() => setLang(l.code)}
            aria-pressed={active}
            className={[
              "has-custom-focus px-2.5 py-1 text-xs font-semibold rounded-pill transition-colors",
              active
                ? "bg-primary text-white"
                : "text-muted hover:text-app",
            ].join(" ")}
          >
            {l.label}
          </button>
        );
      })}
    </div>
  );
}
