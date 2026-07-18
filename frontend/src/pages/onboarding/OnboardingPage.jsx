import { useOnboardingPage } from "./hooks/useOnboardingPage";
import "./onboarding.css";

const today = new Date().toISOString().slice(0, 10);

// Nhãn tiếng Việt giữ nguyên như ProfilePage (FIELD_LABELS) để hai trang nhất quán -
// không tách import chung vì useProfilePage.js/ProfilePage.jsx nằm ngoài phạm vi sửa của trang này.
const FIELD_LABELS = {
  fullName: "Họ và tên",
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
};

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
  const { form, loading, submitting, handleFieldChange, handleSubmit } =
    useOnboardingPage();

  if (loading) {
    return (
      <div className="onboarding-page">
        <div className="onboarding-card">Đang tải hồ sơ...</div>
      </div>
    );
  }

  return (
    <div className="onboarding-page">
      <div className="onboarding-card onboarding-card--wide">
        <h1 className="onboarding-card__title">Hoàn tất hồ sơ</h1>
        <p className="onboarding-card__subtitle">
          Vui lòng cho biết họ tên và ngày sinh để tiếp tục sử dụng hệ thống.
          Các thông tin khác có thể bỏ qua và bổ sung sau tại trang hồ sơ.
        </p>

        {/* noValidate: validation cho fullName/dob tự viết bằng tiếng Việt trong
            useOnboardingPage - để trình duyệt tự chặn bằng bubble mặc định (tiếng Anh)
            thì handleSubmit không bao giờ chạy tới nhánh hiển thị toast lỗi tiếng Việt. */}
        <form className="onboarding-form" onSubmit={handleSubmit} noValidate>
          <div className="onboarding-section">
            <h2 className="onboarding-section__title">Thông tin bắt buộc</h2>
            <div className="onboarding-grid">
              <label className="onboarding-field">
                {FIELD_LABELS.fullName}
                <span className="onboarding-field__required">*</span>
                <input
                  type="text"
                  value={form.fullName ?? ""}
                  onChange={(e) => handleFieldChange("fullName", e.target.value)}
                />
              </label>

              <label className="onboarding-field">
                {FIELD_LABELS.dob}
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
              Thông tin khác <span className="onboarding-section__hint">(có thể bỏ qua)</span>
            </h2>
            <div className="onboarding-grid">
              {OPTIONAL_FIELD_ORDER.map((field) => (
                <label className="onboarding-field" key={field}>
                  {FIELD_LABELS[field]}
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
                      <option value="">-- Chọn --</option>
                      <option value="MALE">Nam</option>
                      <option value="FEMALE">Nữ</option>
                      <option value="OTHER">Khác</option>
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
            {submitting ? "Đang lưu..." : "Tiếp tục"}
          </button>
        </form>
      </div>
    </div>
  );
}
