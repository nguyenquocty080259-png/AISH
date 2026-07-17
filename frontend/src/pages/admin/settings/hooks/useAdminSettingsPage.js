import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

export function useAdminSettingsPage() {
  const { showSuccess, showError } = useToast();

  const [minUploadAge, setMinUploadAge] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const data = await adminApi.getMinUploadAge();
      setMinUploadAge(String(data.minUploadAge));
    } catch (err) {
      showError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const saveMinUploadAge = async (value) => {
    setSaving(true);
    try {
      const data = await adminApi.updateMinUploadAge(Number(value));
      setMinUploadAge(String(data.minUploadAge));
      showSuccess("Đã lưu cài đặt.");
      return true;
    } catch (err) {
      // 400 (ngoài khoảng 6..100) đến từ GlobalExceptionHandler -> hiện đúng message thật.
      showError(err.message);
      return false;
    } finally {
      setSaving(false);
    }
  };

  return {
    minUploadAge,
    loading,
    saving,
    saveMinUploadAge,
  };
}
