import { useProfilePage } from "./hooks/useProfilePage";
import StorageUsageBar from "../../components/ui/StorageUsageBar";
import "./profile.css";
import { useRef } from "react";
import { FaCamera } from "react-icons/fa";
import { MdEmail } from "react-icons/md";

const FIELD_LABELS = {
  fullName: "Họ và tên",
  username: "Tên người dùng",
  email: "Email",
  bio: "Giới thiệu",
  dob: "Ngày sinh",
  gender: "Giới tính",
  phoneNumber: "Số điện thoại",
  university: "Trường",
  faculty: "Khoa",
  major: "Ngành",
  country: "Quốc gia",
  city: "Thành phố",
  githubUrl: "GitHub",
  linkedinUrl: "LinkedIn",
  websiteUrl: "Website",
  trashRetentionDays: "Số ngày giữ tài liệu trong thùng rác",
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

export default function ProfilePage() {
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
    return <div className="profile-page">Đang tải hồ sơ...</div>;
  }

  if (!profile) {
    return <div className="profile-page">Không tải được hồ sơ.</div>;
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
          <h1 className="profile-name">{profile.fullName || "Chưa cập nhật"}</h1>
          {profile.username && (
            <p className="profile-username">@{profile.username}</p>
          )}
        </div>

        

        {!editing && (
          <button className="profile-edit-btn" onClick={startEditing}>
            Chỉnh sửa hồ sơ
          </button>
        )}
      </div>

      {!editing ? (
        <div className="profile-view">
          {profile.bio && <p className="profile-bio">{profile.bio}</p>}
          <div className="profile-grid">
            {Object.keys(FIELD_LABELS)
              .filter((field) => field !== "username")
              .map((field) => (
                <div className="profile-grid__item" key={field}>
                  <span className="profile-grid__label">
                    {FIELD_LABELS[field]}
                  </span>
                  <span className="profile-grid__value">
                    {field === "trashRetentionDays"
                      ? profile[field]
                        ? `${profile[field]} ngày`
                        : "Mặc định (30 ngày)"
                      : profile[field] || "—"}
                  </span>
                </div>
              ))}
          </div>
          <div className="profile-storage">
            <h3 className="profile-storage__title">Dung lượng lưu trữ</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>
        </div>
      ) : (
        <form className="profile-form" onSubmit={handleSave}>
          {Object.keys(FIELD_LABELS)
            .filter((field) => field !== "username")
            .map((field) => (
            <label className="profile-form__field" key={field}>
              {FIELD_LABELS[field]}
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
                  <option value="">-- Chọn --</option>
                  <option value="Nam">Nam</option>
                  <option value="Nữ">Nữ</option>
                  <option value="Khác">Khác</option>
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
                    Tài liệu trong thùng rác tự xóa vĩnh viễn sau số ngày này (mặc định 30, tối đa 90).
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
            <h3 className="profile-storage__title">Dung lượng lưu trữ</h3>
            <StorageUsageBar usage={storageUsage} />
          </div>

          <div className="profile-form__actions">
            <button
              type="button"
              className="profile-form__cancel"
              onClick={cancelEditing}
              disabled={saving}
            >
              Hủy
            </button>
            <button type="submit" className="profile-form__save" disabled={saving}>
              {saving ? "Đang lưu..." : "Lưu thay đổi"}
            </button>
          </div>
        </form>
      )}
    </div>
  );
}
