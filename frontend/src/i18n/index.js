import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import LanguageDetector from "i18next-browser-languagedetector";

import vi from "./locales/vi.json";
import en from "./locales/en.json";

// Ngôn ngữ được hỗ trợ. Tiếng Việt là mặc định + fallback.
export const SUPPORTED_LANGUAGES = ["vi", "en"];
export const DEFAULT_LANGUAGE = "vi";

// Key localStorage ổn định để nhớ lựa chọn ngôn ngữ giữa các phiên.
export const LANGUAGE_STORAGE_KEY = "i18nextLng";

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      vi: { translation: vi },
      en: { translation: en },
    },
    fallbackLng: DEFAULT_LANGUAGE,
    supportedLngs: SUPPORTED_LANGUAGES,
    // Nếu chưa từng chọn ngôn ngữ (localStorage trống) -> mặc định tiếng Việt.
    load: "languageOnly",
    nonExplicitSupportedLngs: true,
    detection: {
      // Chỉ đọc/ghi localStorage, không dùng ngôn ngữ trình duyệt để đảm bảo
      // lần đầu vào (localStorage trống) luôn ra tiếng Việt.
      order: ["localStorage"],
      lookupLocalStorage: LANGUAGE_STORAGE_KEY,
      caches: ["localStorage"],
    },
    interpolation: {
      escapeValue: false, // React đã tự escape.
    },
    react: {
      useSuspense: false,
    },
  });

export default i18n;
