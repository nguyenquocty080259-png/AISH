import { useProfilePage } from "./hooks/useProfilePage";
import { API_ORIGIN } from "../../constants/apiConfig";
import StorageUsageBar from "../../components/ui/StorageUsageBar";
import NotificationSettings from "./components/NotificationSettings";
import Button from "../../components/ui/Button";
import EmptyState from "../../components/ui/EmptyState";
import Skeleton, { SkeletonText } from "../../components/ui/Skeleton";
import { Input, Select, Textarea } from "../../components/ui/Field";
import "./profile.css";
import { useRef } from "react";
import { useTranslation } from "react-i18next";
import { FaCamera } from "react-icons/fa";

const FIELD_ORDER = [
  "fullName", "username", "email", "bio", "dob", "gender", "phoneNumber",
  "university", "faculty", "major", "country", "city",
  "githubUrl", "linkedinUrl", "websiteUrl", "trashRetentionDays",
];

// Giới tính được lưu nguyên văn tiếng Việt ("Nam"/"Nữ"/"Khác") vì <select> ở form gửi thẳng
// chuỗi đó lên BE. Map ngược về key i18n lúc HIỂN THỊ (không đổi giá trị lưu, không đụng BE);
// giá trị lạ từ dữ liệu cũ thì trả về nguyên văn thay vì mất trắng.
const GENDER_I18N_KEYS = {
  Nam: "male",
  "Nữ": "female",
  "Khác": "other",
};

function initials(name) {
  if (!name) return "?";
  return name
    .split(" ")
    .filter(Boolean)
    .slice(-2)
    .map((part) => part[0]?.toUpperCase())
    .join("");
}

// Trang HỒ SƠ CÁ NHÂN: xem/sửa thông tin, đổi ảnh đại diện, thanh dung lượng, và cài đặt thông báo.
export default function ProfilePage() {
  const { t } = useTranslation();
  const fileInputRef = useRef(null);
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

  const displayGender = (value) => {
    const key = GENDER_I18N_KEYS[value?.trim()];
    return key ? t(`common.gender.${key}`) : value;
  };

  if (loading) {
    return (
      <div className="profile-page" aria-busy="true" aria-label={t("profile.loading")}>
        <div className="profile-header">
          <Skeleton variant="circle" width={112} height={112} />
          <div style={{ flex: 1, minWidth: 200 }}>
            <SkeletonText lines={2} />
          </div>
        </div>
        <SkeletonText lines={6} />
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <EmptyState tone="danger" icon="⚠️" title={t("profile.loadError")} />
      </div>
    );
  }

  const avatarSrc =
  avatarPreview ||
  (profile?.avatarUrl
    ? `${API_ORIGIN}${profile.avatarUrl}`
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
                className="profile-avatar__camera has-custom-focus"
                onClick={() => fileInputRef.current?.click()}
                aria-label={t("profile.changeAvatar")}
              >
                <FaCamera />
              </button>
            </>
          )}
        </div>

        <div className="profile-header__text">
          <h1 className="profile-name">{profile.fullName || t("profile.notSet")}</h1>
          {profile.username && (
            <p className="profile-username">@{profile.username}</p>
          )}
        </div>

        {!editing && (
          <div className="profile-header__actions">
            <Button variant="secondary" onClick={startEditing}>
              {t("profile.edit")}
            </Button>
          </div>
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
                      : field === "gender"
                      ? displayGender(profile[field]) || "—"
                      : profile[field] || "—"}
                  </span>
                </div>
              ))}
          </div>
          <div className="profile-storage">
            <h3 className="profile-storage__title">{t("profile.storageTitle")}</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>
          <NotificationSettings />
        </div>
      ) : (
        <form className="profile-form" onSubmit={handleSave}>
          {FIELD_ORDER
            .filter((field) => field !== "username")
            .map((field) => {
              const label = t(`common.fields.${field}`);
              const value = form[field] ?? "";
              const onChange = (e) => handleFieldChange(field, e.target.value);

              if (field === "bio") {
                return (
                  <Textarea
                    key={field}
                    label={label}
                    rows={3}
                    value={value}
                    onChange={onChange}
                    fieldClassName="profile-form__field profile-form__field--wide"
                  />
                );
              }

              if (field === "gender") {
                return (
                  <Select
                    key={field}
                    label={label}
                    value={value}
                    onChange={onChange}
                    fieldClassName="profile-form__field"
                  >
                    <option value="">{t("common.gender.choose")}</option>
                    <option value="Nam">{t("common.gender.male")}</option>
                    <option value="Nữ">{t("common.gender.female")}</option>
                    <option value="Khác">{t("common.gender.other")}</option>
                  </Select>
                );
              }

              if (field === "trashRetentionDays") {
                return (
                  <Input
                    key={field}
                    label={label}
                    type="number"
                    min={1}
                    max={90}
                    value={value}
                    onChange={onChange}
                    hint={t("profile.trashHint")}
                    fieldClassName="profile-form__field"
                  />
                );
              }

              // Email do BE quản lý (đăng nhập), chỉ đọc.
              const readOnly = field === "email";
              return (
                <Input
                  key={field}
                  label={label}
                  type={field === "dob" ? "date" : readOnly ? "email" : "text"}
                  value={value}
                  readOnly={readOnly}
                  onChange={readOnly ? undefined : onChange}
                  fieldClassName="profile-form__field"
                />
              );
            })}

          <div className="profile-storage">
            <h3 className="profile-storage__title">{t("profile.storageTitle")}</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>

          <div className="profile-form__actions">
            <Button variant="secondary" onClick={cancelEditing} disabled={saving}>
              {t("common.actions.cancel")}
            </Button>
            <Button type="submit" loading={saving}>
              {saving ? t("profile.saving") : t("profile.save")}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}
