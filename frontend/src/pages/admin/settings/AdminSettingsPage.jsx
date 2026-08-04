import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import { useAdminSettingsPage } from "./hooks/useAdminSettingsPage";
import "./admin-settings.css";

// Trang Admin CẤU HÌNH HỆ THỐNG: 4 form riêng biệt — tuổi tối thiểu upload, giới hạn dung lượng/
// quota, loại tệp cho phép, và tham số AI (topK/ngưỡng/trọng số gợi ý/kích thước chunk).
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
    aiTopK,
    aiThreshold,
    aiRecentLimit,
    aiSubjectOverlapWeight,
    aiFavoriteWeight,
    aiDownloadWeight,
    aiRatingWeight,
    aiChunkSize,
    savingAiConfig,
    saveAiConfig,
  } = useAdminSettingsPage();
  const [value, setValue] = useState("");
  const [maxFileLocal, setMaxFileLocal] = useState("");
  const [maxFileCloud, setMaxFileCloud] = useState("");
  const [quotaLocal, setQuotaLocal] = useState("");
  const [quotaCloud, setQuotaCloud] = useState("");
  const [fileTypes, setFileTypes] = useState("");
  const [topK, setTopK] = useState("");
  const [threshold, setThreshold] = useState("");
  const [recentLimit, setRecentLimit] = useState("");
  const [subjectOverlapWeight, setSubjectOverlapWeight] = useState("");
  const [favoriteWeight, setFavoriteWeight] = useState("");
  const [downloadWeight, setDownloadWeight] = useState("");
  const [ratingWeight, setRatingWeight] = useState("");
  const [chunkSize, setChunkSize] = useState("");

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

  useEffect(() => {
    setTopK(aiTopK);
  }, [aiTopK]);

  useEffect(() => {
    setThreshold(aiThreshold);
  }, [aiThreshold]);

  useEffect(() => {
    setRecentLimit(aiRecentLimit);
  }, [aiRecentLimit]);

  useEffect(() => {
    setSubjectOverlapWeight(aiSubjectOverlapWeight);
  }, [aiSubjectOverlapWeight]);

  useEffect(() => {
    setFavoriteWeight(aiFavoriteWeight);
  }, [aiFavoriteWeight]);

  useEffect(() => {
    setDownloadWeight(aiDownloadWeight);
  }, [aiDownloadWeight]);

  useEffect(() => {
    setRatingWeight(aiRatingWeight);
  }, [aiRatingWeight]);

  useEffect(() => {
    setChunkSize(aiChunkSize);
  }, [aiChunkSize]);

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

  const aiConfigFieldsFilled =
    topK !== "" &&
    threshold !== "" &&
    recentLimit !== "" &&
    subjectOverlapWeight !== "" &&
    favoriteWeight !== "" &&
    downloadWeight !== "" &&
    ratingWeight !== "" &&
    chunkSize !== "";

  const submitAiConfig = (e) => {
    e.preventDefault();
    if (!aiConfigFieldsFilled) return;
    saveAiConfig({
      topK,
      similarityThreshold: threshold,
      recentMessageLimit: recentLimit,
      subjectOverlapWeight,
      favoriteWeight,
      downloadWeight,
      ratingWeight,
      chunkSize,
    });
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

          <form className="admin-settings-form" onSubmit={submitAiConfig}>
            <h2 className="admin-settings-form__title">{t("admin.settings.aiTitle")}</h2>

            <label className="admin-settings-form__field">
              {t("admin.settings.aiTopK")}
              <input
                type="number"
                min={1}
                max={20}
                value={topK}
                onChange={(e) => setTopK(e.target.value)}
              />
              <span className="admin-settings-form__hint">{t("admin.settings.aiTopKHint")}</span>
            </label>

            <label className="admin-settings-form__field">
              {t("admin.settings.aiThreshold")}
              <input
                type="number"
                min={0}
                max={1}
                step="any"
                value={threshold}
                onChange={(e) => setThreshold(e.target.value)}
              />
              <span className="admin-settings-form__hint">{t("admin.settings.aiThresholdHint")}</span>
            </label>

            <label className="admin-settings-form__field">
              {t("admin.settings.aiRecentLimit")}
              <input
                type="number"
                min={0}
                max={50}
                value={recentLimit}
                onChange={(e) => setRecentLimit(e.target.value)}
              />
              <span className="admin-settings-form__hint">{t("admin.settings.aiRecentLimitHint")}</span>
            </label>

            <label className="admin-settings-form__field">
              {t("admin.settings.aiSubjectOverlapWeight")}
              <input
                type="number"
                min={0}
                step="any"
                value={subjectOverlapWeight}
                onChange={(e) => setSubjectOverlapWeight(e.target.value)}
              />
            </label>
            <label className="admin-settings-form__field">
              {t("admin.settings.aiFavoriteWeight")}
              <input
                type="number"
                min={0}
                step="any"
                value={favoriteWeight}
                onChange={(e) => setFavoriteWeight(e.target.value)}
              />
            </label>
            <label className="admin-settings-form__field">
              {t("admin.settings.aiDownloadWeight")}
              <input
                type="number"
                min={0}
                step="any"
                value={downloadWeight}
                onChange={(e) => setDownloadWeight(e.target.value)}
              />
            </label>
            <label className="admin-settings-form__field">
              {t("admin.settings.aiRatingWeight")}
              <input
                type="number"
                min={0}
                step="any"
                value={ratingWeight}
                onChange={(e) => setRatingWeight(e.target.value)}
              />
              <span className="admin-settings-form__hint">{t("admin.settings.aiWeightHint")}</span>
            </label>

            <label className="admin-settings-form__field">
              {t("admin.settings.aiChunkSize")}
              <input
                type="number"
                min={100}
                max={2000}
                value={chunkSize}
                onChange={(e) => setChunkSize(e.target.value)}
              />
              <span className="admin-settings-form__hint">{t("admin.settings.aiChunkSizeHint")}</span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingAiConfig || !aiConfigFieldsFilled}
              >
                {savingAiConfig ? t("admin.common.saving") : t("common.actions.save")}
              </Button>
            </div>
          </form>
        </>
      )}
    </div>
  );
}
