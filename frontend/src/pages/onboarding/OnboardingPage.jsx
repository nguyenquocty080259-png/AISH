import { useTranslation } from "react-i18next";
import { useOnboardingPage } from "./hooks/useOnboardingPage";
import "./onboarding.css";

const today = new Date().toISOString().slice(0, 10);

const OPTIONAL_FIELD_ORDER = [
  "bio",
  "gender",
  "phoneNumber",
  "university",
  "faculty",
  "major",
  "country",
  "city",
  "githubUrl",
  "linkedinUrl",
  "websiteUrl",
];

export default function OnboardingPage() {
  const { t } = useTranslation();
  const { form, loading, submitting, handleFieldChange, handleSubmit } =
    useOnboardingPage();

  if (loading) {
    return (
      <div className="onboarding-page">
        <div className="onboarding-card">{t("onboarding.loading")}</div>
      </div>
    );
  }

  return (
    <div className="onboarding-page">
      <div className="onboarding-card onboarding-card--wide">
        <h1 className="onboarding-card__title">{t("onboarding.title")}</h1>
        <p className="onboarding-card__subtitle">
          {t("onboarding.subtitle")}
        </p>

        {/* noValidate: validation cho fullName/dob tự viết bằng tiếng Việt trong
            useOnboardingPage - để trình duyệt tự chặn bằng bubble mặc định (tiếng Anh)
            thì handleSubmit không bao giờ chạy tới nhánh hiển thị toast lỗi tiếng Việt. */}
        <form className="onboarding-form" onSubmit={handleSubmit} noValidate>
          <div className="onboarding-section">
            <h2 className="onboarding-section__title">{t("onboarding.requiredSection")}</h2>
            <div className="onboarding-grid">
              <label className="onboarding-field">
                {t("common.fields.fullName")}
                <span className="onboarding-field__required">*</span>
                <input
                  type="text"
                  value={form.fullName ?? ""}
                  onChange={(e) => handleFieldChange("fullName", e.target.value)}
                />
              </label>

              <label className="onboarding-field">
                {t("common.fields.dob")}
                <span className="onboarding-field__required">*</span>
                <input
                  type="date"
                  max={today}
                  value={form.dob ?? ""}
                  onChange={(e) => handleFieldChange("dob", e.target.value)}
                />
              </label>
            </div>
          </div>

          <div className="onboarding-section">
            <h2 className="onboarding-section__title">
              {t("onboarding.otherSection")} <span className="onboarding-section__hint">{t("onboarding.otherHint")}</span>
            </h2>
            <div className="onboarding-grid">
              {OPTIONAL_FIELD_ORDER.map((field) => (
                <label className="onboarding-field" key={field}>
                  {t(`common.fields.${field}`)}
                  {field === "bio" ? (
                    <textarea
                      rows={3}
                      value={form[field] ?? ""}
                      onChange={(e) => handleFieldChange(field, e.target.value)}
                    />
                  ) : field === "gender" ? (
                    <select
                      value={form[field] ?? ""}
                      onChange={(e) => handleFieldChange(field, e.target.value)}
                    >
                      <option value="">{t("common.gender.choose")}</option>
                      <option value="Nam">{t("common.gender.male")}</option>
                      <option value="Nữ">{t("common.gender.female")}</option>
                      <option value="Khác">{t("common.gender.other")}</option>
                    </select>
                  ) : (
                    <input
                      type="text"
                      value={form[field] ?? ""}
                      onChange={(e) => handleFieldChange(field, e.target.value)}
                    />
                  )}
                </label>
              ))}
            </div>
          </div>

          <button type="submit" className="onboarding-submit" disabled={submitting}>
            {submitting ? t("onboarding.submitting") : t("onboarding.submit")}
          </button>
        </form>
      </div>
    </div>
  );
}
