import { useTranslation } from "react-i18next";
import { Input } from "../../../components/ui/Field";

const IconSearch = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <circle cx="11" cy="11" r="8" />
    <path d="m21 21-4.3-4.3" />
  </svg>
);

export default function SearchBar({ value, onChange }) {
  const { t } = useTranslation();
  return (
    <Input
      type="search"
      value={value}
      onChange={(e) => onChange(e.target.value)}
      placeholder={t("documents.searchPlaceholder")}
      aria-label={t("documents.searchPlaceholder")}
      leftIcon={<IconSearch />}
      fieldClassName="doc-search"
    />
  );
}
