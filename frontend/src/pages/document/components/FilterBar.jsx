import { useTranslation } from "react-i18next";

export default function FilterBar({
  subjects,
  subjectFilter,
  onSubjectChange,
  onReset,
}) {
  const { t } = useTranslation();
  return (
    <div className="doc-filterbar">
      <select
        value={subjectFilter}
        onChange={(e) => onSubjectChange(e.target.value)}
      >
        <option value="">{t("documents.allSubjects")}</option>
        {subjects.map((subject) => (
          <option key={subject.id} value={subject.id}>
            {subject.name}
          </option>
        ))}
      </select>

      <button type="button" className="doc-filterbar__reset" onClick={onReset}>
        {t("documents.resetFilter")}
      </button>
    </div>
  );
}