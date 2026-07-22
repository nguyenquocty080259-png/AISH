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
    maxFileLocalGb,
    maxFileCloudGb,
    quotaLocalGb,
    quotaCloudGb,
    savingLimits,
    saveUploadLimits,
    allowedFileTypes,
    savingFileTypes,
    saveAllowedFileTypes,
  } = useAdminSettingsPage();
  const [value, setValue] = useState("");
  const [maxFileLocal, setMaxFileLocal] = useState("");
  const [maxFileCloud, setMaxFileCloud] = useState("");
  const [quotaLocal, setQuotaLocal] = useState("");
  const [quotaCloud, setQuotaCloud] = useState("");
  const [fileTypes, setFileTypes] = useState("");

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

            <p className="admin-settings-form__section-label">Mỗi tệp (áp dụng cho từng lần tải lên)</p>
            <label className="admin-settings-form__field">
              Máy chủ (LOCAL, GB / tệp)
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
              Cloud (CLOUD, GB / tệp)
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={maxFileCloud}
                onChange={(e) => setMaxFileCloud(e.target.value)}
              />
            </label>

            <p className="admin-settings-form__section-label">Tổng dung lượng mỗi người dùng (quota, chung cho mọi user)</p>
            <label className="admin-settings-form__field">
              Máy chủ (LOCAL, GB / người dùng)
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
              Cloud (CLOUD, GB / người dùng)
              <input
                type="number"
                min={0}
                max={2}
                step="any"
                value={quotaCloud}
                onChange={(e) => setQuotaCloud(e.target.value)}
              />
              <span className="admin-settings-form__hint">
                Nhập số GB thập phân (vd. 1, 0.5), tối đa 2 GB cho mỗi giá trị. Giới hạn mỗi tệp
                không được vượt quá quota tương ứng. Tài liệu trong thùng rác vẫn tính vào quota
                cho tới khi bị xóa vĩnh viễn. Tài liệu lưu "Cả hai" chiếm quota ở cả hai nơi.
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingLimits || !limitsFieldsFilled}
              >
                {savingLimits ? "Đang lưu..." : "Lưu"}
              </Button>
            </div>
          </form>

          <form className="admin-settings-form" onSubmit={submitFileTypes}>
            <h2 className="admin-settings-form__title">Loại tệp được phép tải lên</h2>
            <label className="admin-settings-form__field">
              Danh sách đuôi tệp (ngăn cách bằng dấu phẩy)
              <textarea
                rows={3}
                value={fileTypes}
                onChange={(e) => setFileTypes(e.target.value)}
                placeholder="pdf, docx, png, jpg"
              />
              <span className="admin-settings-form__hint">
                Chỉ những đuôi tệp trong danh sách này mới được tải lên (áp dụng cho mọi người
                dùng). Không kèm dấu chấm, mỗi đuôi tối đa 12 ký tự chữ thường/số. Hệ thống còn
                kiểm tra nội dung thật của tệp để chống đổi đuôi giả. Thay đổi có hiệu lực ngay,
                không cần khởi động lại.
              </span>
            </label>

            <div className="admin-settings-form__actions">
              <Button
                type="submit"
                variant="primary"
                disabled={savingFileTypes || fileTypes.trim() === ""}
              >
                {savingFileTypes ? "Đang lưu..." : "Lưu"}
              </Button>
            </div>
          </form>
        </>
      )}
    </div>
  );
}
