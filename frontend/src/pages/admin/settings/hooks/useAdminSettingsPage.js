import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

// 1 GB = 1024^3 byte - quy đổi chỉ tồn tại ở FE, API/DB luôn dùng byte (xem adminApi.js).
const GB_BYTES = 1024 ** 3;
const MAX_LIMIT_GB = 2; // trần cứng spring.servlet.multipart.max-file-size (2GB) - áp dụng cho cả 4 giá trị

function bytesToGb(bytes) {
  // Làm tròn 6 chữ số thập phân rồi bỏ số 0 thừa, để giá trị load lên hiển thị gọn
  // (vd. 1073741824 -> "1", không phải "1.000000").
  return String(Math.round((bytes / GB_BYTES) * 1e6) / 1e6);
}

export function useAdminSettingsPage() {
  const { showSuccess, showError } = useToast();

  const [minUploadAge, setMinUploadAge] = useState("");
  const [maxFileLocalGb, setMaxFileLocalGb] = useState("");
  const [maxFileCloudGb, setMaxFileCloudGb] = useState("");
  const [quotaLocalGb, setQuotaLocalGb] = useState("");
  const [quotaCloudGb, setQuotaCloudGb] = useState("");
  const [allowedFileTypes, setAllowedFileTypes] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingLimits, setSavingLimits] = useState(false);
  const [savingFileTypes, setSavingFileTypes] = useState(false);

  const load = async () => {
    setLoading(true);
    try {
      const [ageData, limitsData, fileTypesData] = await Promise.all([
        adminApi.getMinUploadAge(),
        adminApi.getUploadLimits(),
        adminApi.getUploadFileTypes(),
      ]);
      setMinUploadAge(String(ageData.minUploadAge));
      setMaxFileLocalGb(bytesToGb(limitsData.maxFileLocalBytes));
      setMaxFileCloudGb(bytesToGb(limitsData.maxFileCloudBytes));
      setQuotaLocalGb(bytesToGb(limitsData.quotaLocalBytes));
      setQuotaCloudGb(bytesToGb(limitsData.quotaCloudBytes));
      setAllowedFileTypes((fileTypesData.allowedExtensions || []).join(", "));
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

  // Validate ở FE trước cho phản hồi tức thì; validate thật ở BE (> 0, <= 2GB, tệp <= quota)
  // vẫn là nguồn sự thật cuối cùng.
  const validateGbValue = (value, label) => {
    const num = Number(value);
    if (value === "" || Number.isNaN(num)) {
      return `Giá trị ${label} phải là một số.`;
    }
    if (num <= 0) {
      return `Giá trị ${label} phải lớn hơn 0.`;
    }
    if (num > MAX_LIMIT_GB) {
      return `Giá trị ${label} không được vượt quá ${MAX_LIMIT_GB} GB.`;
    }
    return null;
  };

  const saveUploadLimits = async (values) => {
    const { maxFileLocalGbValue, maxFileCloudGbValue, quotaLocalGbValue, quotaCloudGbValue } = values;

    const checks = [
      [maxFileLocalGbValue, "giới hạn tệp LOCAL"],
      [maxFileCloudGbValue, "giới hạn tệp CLOUD"],
      [quotaLocalGbValue, "quota LOCAL"],
      [quotaCloudGbValue, "quota CLOUD"],
    ];
    for (const [value, label] of checks) {
      const error = validateGbValue(value, label);
      if (error) {
        showError(error);
        return false;
      }
    }
    if (Number(maxFileLocalGbValue) > Number(quotaLocalGbValue)) {
      showError("Giới hạn dung lượng tệp LOCAL không được vượt quá quota LOCAL.");
      return false;
    }
    if (Number(maxFileCloudGbValue) > Number(quotaCloudGbValue)) {
      showError("Giới hạn dung lượng tệp CLOUD không được vượt quá quota CLOUD.");
      return false;
    }

    setSavingLimits(true);
    try {
      const data = await adminApi.updateUploadLimits({
        maxFileLocalBytes: Math.round(Number(maxFileLocalGbValue) * GB_BYTES),
        maxFileCloudBytes: Math.round(Number(maxFileCloudGbValue) * GB_BYTES),
        quotaLocalBytes: Math.round(Number(quotaLocalGbValue) * GB_BYTES),
        quotaCloudBytes: Math.round(Number(quotaCloudGbValue) * GB_BYTES),
      });
      setMaxFileLocalGb(bytesToGb(data.maxFileLocalBytes));
      setMaxFileCloudGb(bytesToGb(data.maxFileCloudBytes));
      setQuotaLocalGb(bytesToGb(data.quotaLocalBytes));
      setQuotaCloudGb(bytesToGb(data.quotaCloudBytes));
      showSuccess("Đã lưu cài đặt.");
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setSavingLimits(false);
    }
  };

  // Parse chuỗi admin nhập (ngăn cách bằng dấu phẩy/khoảng trắng/xuống dòng) thành mảng đuôi
  // chuẩn hoá: viết thường, bỏ dấu chấm đầu, bỏ trùng, giữ thứ tự. BE cũng chuẩn hoá lại lần nữa.
  const parseExtensions = (raw) => {
    const seen = new Set();
    const result = [];
    for (const part of String(raw).split(/[\s,]+/)) {
      let cleaned = part.trim().toLowerCase();
      if (cleaned.startsWith(".")) cleaned = cleaned.slice(1);
      if (cleaned && !seen.has(cleaned)) {
        seen.add(cleaned);
        result.push(cleaned);
      }
    }
    return result;
  };

  const saveAllowedFileTypes = async (raw) => {
    const extensions = parseExtensions(raw);
    if (extensions.length === 0) {
      showError("Danh sách loại tệp được phép không được để trống.");
      return false;
    }
    const invalid = extensions.find((ext) => !/^[a-z0-9]{1,12}$/.test(ext));
    if (invalid) {
      showError(`Đuôi tệp '${invalid}' không hợp lệ - chỉ gồm chữ thường/số, tối đa 12 ký tự.`);
      return false;
    }

    setSavingFileTypes(true);
    try {
      const data = await adminApi.updateUploadFileTypes(extensions);
      setAllowedFileTypes((data.allowedExtensions || []).join(", "));
      showSuccess("Đã lưu cài đặt.");
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setSavingFileTypes(false);
    }
  };

  return {
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
  };
}
