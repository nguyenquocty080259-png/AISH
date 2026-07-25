import { useTranslation } from "react-i18next";

export default function SearchBar({ value, onChange }) {
  const { t } = useTranslation();
  return (
    <input
      className="doc-search"
      type="text"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={t("documents.searchPlaceholder")}
    />
  );
}
