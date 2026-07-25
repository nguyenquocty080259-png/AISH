import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import { useAdminSettingsPage } from "./hooks/useAdminSettingsPage";
import "./admin-settings.css";

export default function AdminSettingsPage() {
  const { t } = useTranslation();
  const {
    minUploadAge,
    loading,
    saving,
    saveMinUploadAge,
    maxFileLocalGb,
    maxFileCloudGb,
    quotaLocalGb,
    quotaCloudGb,
    savingLimits,
    saveUploadLimits,
    allowedFileTypes,
    savingFileTypes,
    saveAllowedFileTypes,
  } = useAdminSettingsPage();
  const [value, setValue] = useState("");
  const [maxFileLocal, setMaxFileLocal] = useState("");
  const [maxFileCloud, setMaxFileCloud] = useState("");
  const [quotaLocal, setQuotaLocal] = useState("");
  const [quotaCloud, setQuotaCloud] = useState("");
  const [fileTypes, setFileTypes] = useState("");

  useEffect(() => {
    setValue(minUploadAge);
  }, [minUploadAge]);

  useEffect(() => {
    setMaxFileLocal(maxFileLocalGb);
  }, [maxFileLocalGb]);

  useEffect(() => {
    setMaxFileCloud(maxFileCloudGb);
  }, [maxFileCloudGb]);

  useEffect(() => {
    setQuotaLocal(quotaLocalGb);
  }, [quotaLocalGb]);

  useEffect(() => {
    setQuotaCloud(quotaCloudGb);
  }, [quotaCloudGb]);

  useEffect(() => {
    setFileTypes(allowedFileTypes);
  }, [allowedFileTypes]);

  const submit = (e) => {
    e.preventDefault();
    if (value === "") return;
    saveMinUploadAge(value);
  };

  const limitsFieldsFilled =
    maxFileLocal !== "" && maxFileCloud !== "" && quotaLocal !== "" && quotaCloud !== "";

  const submitLimits = (e) => {
    e.preventDefault();
    if (!limitsFieldsFilled) return;
    saveUploadLimits({
      maxFileLocalGbValue: maxFileLocal,
      maxFileCloudGbValue: maxFileCloud,
      quotaLocalGbValue: quotaLocal,
      quotaCloudGbValue: quotaCloud,
    });
  };

  const submitFileTypes = (e) => {
    e.preventDefault();
    if (fileTypes.trim() === "") return;
    saveAllowedFileTypes(fileTypes);
  };

  return (
    <div className="admin-settings-page">
      <PageHeader
        title={t("admin.settings.title")}
        subtitle={t("admin.settings.subtitle")}
      />

      {loading ? (
        <p className="admin-settings-page__loading">{t("admin.settings.loading")}</p>
      ) : (
        <>
          <form className="admin-settings-form" onSubmit={submit}>
            <h2 className="admin-settings-form__title">{t("admin.settings.ageTitle")}</h2>
            <label className="admin-settings-form__field">
              {t("admin.settings.ageLabel")}
              <input
                type="number"
                min={6}
                max={100}
                value={value}
                onChange={(e) => setValue(e.target.value)}
              />
              <span className="admin-settings-form__hint">
                {t("admin.settings.ageHint")}
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button type="submit" variant="primary" disabled={saving || value === ""}>
                {saving ? t("admin.common.saving") : t("common.actions.save")}
              </Button>
            </div>
          </form>

          <form className="admin-settings-form" onSubmit={submitLimits}>
            <h2 className="admin-settings-form__title">{t("admin.settings.limitsTitle")}</h2>

            <p className="admin-settings-form__section-label">{t("admin.settings.perFileLabel")}</p>
            <label className="admin-settings-form__field">
              {t("admin.settings.maxLocal")}
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={maxFileLocal}
                onChange={(e) => setMaxFileLocal(e.target.value)}
              />
            </label>
            <label className="admin-settings-form__field">
              {t("admin.settings.maxCloud")}
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={maxFileCloud}
                onChange={(e) => setMaxFileCloud(e.target.value)}
              />
            </label>

            <p className="admin-settings-form__section-label">{t("admin.settings.quotaLabel")}</p>
            <label className="admin-settings-form__field">
              {t("admin.settings.quotaLocal")}
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={quotaLocal}
                onChange={(e) => setQuotaLocal(e.target.value)}
              />
            </label>
            <label className="admin-settings-form__field">
              {t("admin.settings.quotaCloud")}
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={quotaCloud}
                onChange={(e) => setQuotaCloud(e.target.value)}
              />
              <span className="admin-settings-form__hint">
                {t("admin.settings.quotaHint")}
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingLimits || !limitsFieldsFilled}
              >
                {savingLimits ? t("admin.common.saving") : t("common.actions.save")}
              </Button>
            </div>
          </form>

          <form className="admin-settings-form" onSubmit={submitFileTypes}>
            <h2 className="admin-settings-form__title">{t("admin.settings.fileTypesTitle")}</h2>
            <label className="admin-settings-form__field">
              {t("admin.settings.fileTypesLabel")}
              <textarea
                rows={3}
                value={fileTypes}
                onChange={(e) => setFileTypes(e.target.value)}
                placeholder="pdf, docx, png, jpg"
              />
              <span className="admin-settings-form__hint">
                {t("admin.settings.fileTypesHint")}
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingFileTypes || fileTypes.trim() === ""}
              >
                {savingFileTypes ? t("admin.common.saving") : t("common.actions.save")}
              </Button>
            </div>
          </form>
        </>
      )}
    </div>
  );
}
