import { useEffect, useState } from "react";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import { useAdminSettingsPage } from "./hooks/useAdminSettingsPage";
import "./admin-settings.css";

export default function AdminSettingsPage() {
  const { minUploadAge, loading, saving, saveMinUploadAge } = useAdminSettingsPage();
  const [value, setValue] = useState("");

  useEffect(() => {
    setValue(minUploadAge);
  }, [minUploadAge]);

  const submit = (e) => {
    e.preventDefault();
    if (value === "") return;
    saveMinUploadAge(value);
  };

  return (
    <div className="admin-settings-page">
      <PageHeader
        title="Cài đặt"
        subtitle="Cấu hình các tham số vận hành chung của hệ thống."
      />

      {loading ? (
        <p className="admin-settings-page__loading">Đang tải cài đặt...</p>
      ) : (
        <form className="admin-settings-form" onSubmit={submit}>
          <label className="admin-settings-form__field">
            Tuổi tối thiểu để tải tài liệu lên
            <input
              type="number"
              min={6}
              max={100}
              value={value}
              onChange={(e) => setValue(e.target.value)}
            />
            <span className="admin-settings-form__hint">
              Người dùng chưa đủ tuổi này (tính từ ngày sinh trong hồ sơ) sẽ không thể tải tài
              liệu lên. Quản trị viên không bị áp dụng giới hạn này.
            </span>
          </label>

          <div className="admin-settings-form__actions">
            <Button type="submit" variant="primary" disabled={saving || value === ""}>
              {saving ? "Đang lưu..." : "Lưu"}
            </Button>
          </div>
        </form>
      )}
    </div>
  );
}
