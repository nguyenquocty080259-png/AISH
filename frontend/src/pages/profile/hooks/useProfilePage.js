import { useEffect, useState } from "react";
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

export function useProfilePage() {
  const { showSuccess, showError } = useToast();

  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({});
  const [saving, setSaving] = useState(false);
  // null = chưa tải xong / lỗi -> StorageUsageBar tự ẩn, không phải lỗi hiển thị (fail-open).
  const [storageUsage, setStorageUsage] = useState(null);

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
    setForm(toFormValues(profile));
    setEditing(true);
  };

  const cancelEditing = () => setEditing(false);

  const handleFieldChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        trashRetentionDays:
          form.trashRetentionDays === "" || form.trashRetentionDays == null
            ? null
            : Number(form.trashRetentionDays),
      };
      const updated = await profileApi.updateMyProfile(payload);
      setProfile(updated);
      setEditing(false);
      showSuccess("Đã cập nhật hồ sơ.");
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
    storageUsage,
  };
}
