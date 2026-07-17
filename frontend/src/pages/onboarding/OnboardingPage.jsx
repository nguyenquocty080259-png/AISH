import { useOnboardingPage } from "./hooks/useOnboardingPage";
import "./onboarding.css";

const today = new Date().toISOString().slice(0, 10);

export default function OnboardingPage() {
  const { dob, setDob, submitting, handleSubmit } = useOnboardingPage();

  return (
    <div className="onboarding-page">
      <div className="onboarding-card">
        <h1 className="onboarding-card__title">Hoàn tất hồ sơ</h1>
        <p className="onboarding-card__subtitle">
          Vui lòng cho biết ngày sinh của bạn để tiếp tục sử dụng hệ thống.
        </p>

        <form className="onboarding-form" onSubmit={handleSubmit}>
          <label className="onboarding-field">
            Ngày sinh
            <input
              type="date"
              required
              max={today}
              value={dob}
              onChange={(e) => setDob(e.target.value)}
            />
          </label>

          <button type="submit" className="onboarding-submit" disabled={submitting}>
            {submitting ? "Đang lưu..." : "Tiếp tục"}
          </button>
        </form>
      </div>
    </div>
  );
}
