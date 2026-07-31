import { useTranslation } from "react-i18next";
import { Select } from "../../../components/ui/Field";
import Button from "../../../components/ui/Button";

export default function FilterBar({
  subjects,
  subjectFilter,
  onSubjectChange,
  onReset,
}) {
  const { t } = useTranslation();
  return (
    <div className="doc-filterbar">
      <Select
        value={subjectFilter}
        onChange={(e) => onSubjectChange(e.target.value)}
        aria-label={t("documents.allSubjects")}
        fieldClassName="doc-filterbar__select"
      >
        <option value="">{t("documents.allSubjects")}</option>
        {subjects.map((subject) => (
          <option key={subject.id} value={subject.id}>
            {subject.name}
          </option>
        ))}
      </Select>

      <Button variant="ghost" onClick={onReset}>
        {t("documents.resetFilter")}
      </Button>
    </div>
  );
}
