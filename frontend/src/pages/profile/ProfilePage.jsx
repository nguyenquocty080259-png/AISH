import { useProfilePage } from "./hooks/useProfilePage";
import "./profile.css";

const FIELD_LABELS = {
  fullName: "Họ và tên",
  username: "Tên người dùng",
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
  } = useProfilePage();

  if (loading) {
    return <div className="profile-page">Đang tải hồ sơ...</div>;
  }

  if (!profile) {
    return <div className="profile-page">Không tải được hồ sơ.</div>;
  }

  return (
    <div className="profile-page">
      <div className="profile-header">
        <div className="profile-avatar">
          {profile.avatarUrl ? (
            <img src={profile.avatarUrl} alt={profile.fullName} />
          ) : (
            <span>{initials(profile.fullName)}</span>
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
              .filter((field) => field !== "fullName" && field !== "bio")
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
                  <option value="MALE">Nam</option>
                  <option value="FEMALE">Nữ</option>
                  <option value="OTHER">Khác</option>
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
                  type="text"
                  value={form[field] ?? ""}
                  onChange={(e) => handleFieldChange(field, e.target.value)}
                />
              )}
            </label>
          ))}

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
