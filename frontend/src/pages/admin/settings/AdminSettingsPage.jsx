import { useEffect, useState } from "react";
import PageHeader from "../../../components/ui/PageHeader";
import Button from "../../../components/ui/Button";
import { useAdminSettingsPage } from "./hooks/useAdminSettingsPage";
import "./admin-settings.css";

export default function AdminSettingsPage() {
  const {
    minUploadAge,
    loading,
    saving,
    saveMinUploadAge,
    uploadLimitLocalGb,
    uploadLimitCloudGb,
    savingLimits,
    saveUploadLimits,
  } = useAdminSettingsPage();
  const [value, setValue] = useState("");
  const [localGb, setLocalGb] = useState("");
  const [cloudGb, setCloudGb] = useState("");

  useEffect(() => {
    setValue(minUploadAge);
  }, [minUploadAge]);

  useEffect(() => {
    setLocalGb(uploadLimitLocalGb);
  }, [uploadLimitLocalGb]);

  useEffect(() => {
    setCloudGb(uploadLimitCloudGb);
  }, [uploadLimitCloudGb]);

  const submit = (e) => {
    e.preventDefault();
    if (value === "") return;
    saveMinUploadAge(value);
  };

  const submitLimits = (e) => {
    e.preventDefault();
    if (localGb === "" || cloudGb === "") return;
    saveUploadLimits(localGb, cloudGb);
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
        <>
          <form className="admin-settings-form" onSubmit={submit}>
            <h2 className="admin-settings-form__title">Độ tuổi tải tài liệu lên</h2>
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

          <form className="admin-settings-form" onSubmit={submitLimits}>
            <h2 className="admin-settings-form__title">Giới hạn dung lượng tải lên</h2>
            <label className="admin-settings-form__field">
              Giới hạn dung lượng tải lên - Máy chủ (LOCAL, GB)
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={localGb}
                onChange={(e) => setLocalGb(e.target.value)}
              />
            </label>

            <label className="admin-settings-form__field">
              Giới hạn dung lượng tải lên - Cloud (CLOUD, GB)
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={cloudGb}
                onChange={(e) => setCloudGb(e.target.value)}
              />
              <span className="admin-settings-form__hint">
                Nhập số GB thập phân (vd. 1, 0.5). Áp dụng riêng cho từng nơi lưu; tài liệu lưu
                "Cả hai" phải đạt cả hai giới hạn. Tối đa 2 GB.
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingLimits || localGb === "" || cloudGb === ""}
              >
                {savingLimits ? "Đang lưu..." : "Lưu"}
              </Button>
            </div>
          </form>
        </>
      )}
    </div>
  );
}
