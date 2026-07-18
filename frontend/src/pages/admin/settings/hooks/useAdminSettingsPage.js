import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

// 1 GB = 1024^3 byte - quy đổi chỉ tồn tại ở FE, API/DB luôn dùng byte (xem adminApi.js).
const GB_BYTES = 1024 ** 3;
const MAX_LIMIT_GB = 2; // trần cứng spring.servlet.multipart.max-file-size (2GB)

function bytesToGb(bytes) {
  // Làm tròn 6 chữ số thập phân rồi bỏ số 0 thừa, để giá trị load lên hiển thị gọn
  // (vd. 1073741824 -> "1", không phải "1.000000").
  return String(Math.round((bytes / GB_BYTES) * 1e6) / 1e6);
}

export function useAdminSettingsPage() {
  const { showSuccess, showError } = useToast();

  const [minUploadAge, setMinUploadAge] = useState("");
  const [uploadLimitLocalGb, setUploadLimitLocalGb] = useState("");
  const [uploadLimitCloudGb, setUploadLimitCloudGb] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingLimits, setSavingLimits] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [ageData, limitsData] = await Promise.all([
        adminApi.getMinUploadAge(),
        adminApi.getUploadLimits(),
      ]);
      setMinUploadAge(String(ageData.minUploadAge));
      setUploadLimitLocalGb(bytesToGb(limitsData.maxUploadLocalBytes));
      setUploadLimitCloudGb(bytesToGb(limitsData.maxUploadCloudBytes));
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

  // Validate ở FE trước cho phản hồi tức thì; validate thật ở BE (> 0 và <= 2GB) vẫn là
  // nguồn sự thật cuối cùng.
  const validateGbValue = (value, label) => {
    const num = Number(value);
    if (value === "" || Number.isNaN(num)) {
      return `Giới hạn dung lượng ${label} phải là một số.`;
    }
    if (num <= 0) {
      return `Giới hạn dung lượng ${label} phải lớn hơn 0.`;
    }
    if (num > MAX_LIMIT_GB) {
      return `Giới hạn dung lượng ${label} không được vượt quá ${MAX_LIMIT_GB} GB.`;
    }
    return null;
  };

  const saveUploadLimits = async (localGbValue, cloudGbValue) => {
    const localError = validateGbValue(localGbValue, "LOCAL");
    if (localError) {
      showError(localError);
      return false;
    }
    const cloudError = validateGbValue(cloudGbValue, "CLOUD");
    if (cloudError) {
      showError(cloudError);
      return false;
    }

    setSavingLimits(true);
    try {
      const data = await adminApi.updateUploadLimits({
        maxUploadLocalBytes: Math.round(Number(localGbValue) * GB_BYTES),
        maxUploadCloudBytes: Math.round(Number(cloudGbValue) * GB_BYTES),
      });
      setUploadLimitLocalGb(bytesToGb(data.maxUploadLocalBytes));
      setUploadLimitCloudGb(bytesToGb(data.maxUploadCloudBytes));
      showSuccess("Đã lưu cài đặt.");
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setSavingLimits(false);
    }
  };

  return {
    minUploadAge,
    loading,
    saving,
    saveMinUploadAge,
    uploadLimitLocalGb,
    uploadLimitCloudGb,
    savingLimits,
    saveUploadLimits,
  };
}
