import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import * as profileApi from "../../../api/profileApi";
import * as documentApi from "../../../api/documentApi";
import { useToast } from "../../../hooks/useToast";


const EDITABLE_FIELDS = [
  "fullName",
  "bio",
  "dob",
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
  "trashRetentionDays",
];

function toFormValues(profile) {
  const values = {};
  EDITABLE_FIELDS.forEach((field) => {
    values[field] = profile?.[field] ?? "";
  });
  return values;
}

// Hook logic trang Hồ sơ: nạp hồ sơ + dung lượng đã dùng, bật chế độ sửa, đổi ảnh đại diện
// (preview tức thì bằng object URL trong lúc tải lên), và lưu thay đổi.
export function useProfilePage() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  // null = chưa tải xong / lỗi -> StorageUsageBar tự ẩn, không phải lỗi hiển thị (fail-open).
  const [storageUsage, setStorageUsage] = useState(null);
  const [avatarPreview, setAvatarPreview] = useState(null);

  // Nạp hồ sơ + dung lượng đã dùng (2 API độc lập, dung lượng lỗi thì thanh dung lượng tự ẩn).
  const load = async () => {
    setLoading(true);
    try {
      const data = await profileApi.getMyProfile();
      setProfile(data);
    } catch (err) {
      showError(err.message);
    } finally {
      setLoading(false);
    }
    documentApi
      .getStorageUsage()
      .then(setStorageUsage)
      .catch(() => setStorageUsage(null));
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const startEditing = () => {
    setForm({
    ...toFormValues(profile),
    email: profile.email,
    });
    setEditing(true);
  };

  const cancelEditing = () => setEditing(false);

  const handleFieldChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  // Đổi ảnh đại diện: hiện preview NGAY bằng object URL (chưa cần chờ server), gọi API tải lên,
  // rồi thay preview bằng URL thật trả về từ server; luôn dọn object URL để không rò rỉ bộ nhớ.
  const handleAvatarChange = async (event) => {
      const file = event.target.files?.[0];

      if (!file) return;

      const previewUrl = URL.createObjectURL(file);

      setAvatarPreview(previewUrl);

      try {
        const response = await profileApi.uploadAvatar(file);

        setProfile((prev) => ({
          ...prev,
          avatarUrl: response.avatarUrl,
        }));

        showSuccess(t("profile.avatarUpdated"));

      } catch (err) {
        showError(err.message);

      } finally {
        URL.revokeObjectURL(previewUrl);
        setAvatarPreview(null);

        // Cho phép chọn lại cùng một file
        event.target.value = "";
      }
    };
  
  // Lưu toàn bộ hồ sơ (trừ email, chỉ đọc). trashRetentionDays rỗng thì gửi null (dùng mặc định 30 ngày ở BE).
  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const { email, ...payload } = form;

        payload.trashRetentionDays =
          payload.trashRetentionDays === "" || payload.trashRetentionDays == null
            ? null
            : Number(payload.trashRetentionDays);

      const updated = await profileApi.updateMyProfile(payload);
      setProfile(updated);
      setEditing(false);
      showSuccess(t("profile.profileUpdated"));
    } catch (err) {
      showError(err.message);
    } finally {
      setSaving(false);
    }
  };

  return {
    profile,
    loading,
    editing,
    form,
    saving,
    startEditing,
    cancelEditing,
    handleFieldChange,
    handleSave,

    avatarPreview,
    handleAvatarChange,

    storageUsage,
};
}
