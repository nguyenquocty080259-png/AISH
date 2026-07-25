import { useProfilePage } from "./hooks/useProfilePage";
import StorageUsageBar from "../../components/ui/StorageUsageBar";
import "./profile.css";
import { useRef } from "react";
import { useTranslation } from "react-i18next";
import { FaCamera } from "react-icons/fa";
import { MdEmail } from "react-icons/md";

const FIELD_ORDER = [
  "fullName", "username", "email", "bio", "dob", "gender", "phoneNumber",
  "university", "faculty", "major", "country", "city",
  "githubUrl", "linkedinUrl", "websiteUrl", "trashRetentionDays",
];

function initials(name) {
  if (!name) return "?";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

export default function ProfilePage() {
  const { t } = useTranslation();
  const fileInputRef = useRef(null);
  const API_BASE = "http://localhost:8080";
  const {
      profile,
      loading,
      editing,
      form,
      saving,
      startEditing,
      cancelEditing,
      handleFieldChange,
      handleSave,
      storageUsage,

      avatarPreview,
      handleAvatarChange,
  } = useProfilePage();

  if (loading) {
    return <div className="profile-page">{t("profile.loading")}</div>;
  }

  if (!profile) {
    return <div className="profile-page">{t("profile.loadError")}</div>;
  }

  const avatarSrc =
  avatarPreview ||
  (profile?.avatarUrl
    ? `${API_BASE}${profile.avatarUrl}`
    : null);

  return (
    <div className="profile-page">
      <div className="profile-header">
        <div className="profile-avatar">
          {avatarPreview || profile.avatarUrl ? (
            <img
                src={avatarSrc}
                alt={profile.fullName}
                className="profile-avatar__image"
            />
          ) : (
            <div className="profile-avatar__placeholder">
              {initials(profile.fullName)}
            </div>
          )}

          {editing && (
            <>
              <input
                ref={fileInputRef}
                type="file"
                accept="image/png,image/jpeg,image/jpg,image/webp"
                style={{ display: "none" }}
                onChange={handleAvatarChange}
              />

              <button
                type="button"
                className="profile-avatar__camera"
                onClick={() => fileInputRef.current?.click()}
              >
                <FaCamera />
              </button>
            </>
          )}
        </div>
        
        <div>
          <h1 className="profile-name">{profile.fullName || t("profile.notSet")}</h1>
          {profile.username && (
            <p className="profile-username">@{profile.username}</p>
          )}
        </div>

        

        {!editing && (
          <button className="profile-edit-btn" onClick={startEditing}>
            {t("profile.edit")}
          </button>
        )}
      </div>

      {!editing ? (
        <div className="profile-view">
          {profile.bio && <p className="profile-bio">{profile.bio}</p>}
          <div className="profile-grid">
            {FIELD_ORDER
              .filter((field) => field !== "username")
              .map((field) => (
                <div className="profile-grid__item" key={field}>
                  <span className="profile-grid__label">
                    {t(`common.fields.${field}`)}
                  </span>
                  <span className="profile-grid__value">
                    {field === "trashRetentionDays"
                      ? profile[field]
                        ? t("profile.daysValue", { count: profile[field] })
                        : t("profile.trashDefault")
                      : profile[field] || "—"}
                  </span>
                </div>
              ))}
          </div>
          <div className="profile-storage">
            <h3 className="profile-storage__title">{t("profile.storageTitle")}</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>
        </div>
      ) : (
        <form className="profile-form" onSubmit={handleSave}>
          {FIELD_ORDER
            .filter((field) => field !== "username")
            .map((field) => (
            <label className="profile-form__field" key={field}>
              {t(`common.fields.${field}`)}
              {field === "bio" ? (
                <textarea
                  rows={3}
                  value={form[field] ?? ""}
                  onChange={(e) => handleFieldChange(field, e.target.value)}
                />
              ) : field === "dob" ? (
                <input
                  type="date"
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
              ) : field === "trashRetentionDays" ? (
                <>
                  <input
                    type="number"
                    min={1}
                    max={90}
                    value={form[field] ?? ""}
                    onChange={(e) => handleFieldChange(field, e.target.value)}
                  />
                  <span className="profile-form__hint">
                    {t("profile.trashHint")}
                  </span>
                </>
              ) : (
              <input
                type={field === "email" ? "email" : "text"}
                value={form[field] ?? ""}
                readOnly={field === "email"}
                onChange={
                  field === "email"
                    ? undefined
                    : (e) => handleFieldChange(field, e.target.value)
                }
              />
            )}
            </label>
          ))}

          <div className="profile-storage">
            <h3 className="profile-storage__title">{t("profile.storageTitle")}</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>

          <div className="profile-form__actions">
            <button
              type="button"
              className="profile-form__cancel"
              onClick={cancelEditing}
              disabled={saving}
            >
              {t("common.actions.cancel")}
            </button>
            <button type="submit" className="profile-form__save" disabled={saving}>
              {saving ? t("profile.saving") : t("profile.save")}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
