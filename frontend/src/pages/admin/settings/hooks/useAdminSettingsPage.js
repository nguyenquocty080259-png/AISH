import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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

// Hook logic trang Admin CẤU HÌNH HỆ THỐNG: 4 form độc lập (tuổi tối thiểu, giới hạn dung lượng,
// loại tệp cho phép, tham số AI) — mỗi form tự validate ở FE trước khi gọi API lưu.
export function useAdminSettingsPage() {
  const { t } = useTranslation();
  const { showSuccess, showError } = useToast();

  const [minUploadAge, setMinUploadAge] = useState("");
  const [maxFileLocalGb, setMaxFileLocalGb] = useState("");
  const [maxFileCloudGb, setMaxFileCloudGb] = useState("");
  const [quotaLocalGb, setQuotaLocalGb] = useState("");
  const [quotaCloudGb, setQuotaCloudGb] = useState("");
  const [allowedFileTypes, setAllowedFileTypes] = useState("");
  const [aiTopK, setAiTopK] = useState("");
  const [aiThreshold, setAiThreshold] = useState("");
  const [aiRecentLimit, setAiRecentLimit] = useState("");
  const [aiSubjectOverlapWeight, setAiSubjectOverlapWeight] = useState("");
  const [aiFavoriteWeight, setAiFavoriteWeight] = useState("");
  const [aiDownloadWeight, setAiDownloadWeight] = useState("");
  const [aiRatingWeight, setAiRatingWeight] = useState("");
  const [aiChunkSize, setAiChunkSize] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [savingLimits, setSavingLimits] = useState(false);
  const [savingFileTypes, setSavingFileTypes] = useState(false);
  const [savingAiConfig, setSavingAiConfig] = useState(false);

  // Nạp cả 4 nhóm cấu hình song song, quy đổi byte -> GB để hiển thị.
  const load = async () => {
    setLoading(true);
    try {
      const [ageData, limitsData, fileTypesData, aiConfigData] = await Promise.all([
        adminApi.getMinUploadAge(),
        adminApi.getUploadLimits(),
        adminApi.getUploadFileTypes(),
        adminApi.getAiConfig(),
      ]);
      setMinUploadAge(String(ageData.minUploadAge));
      setMaxFileLocalGb(bytesToGb(limitsData.maxFileLocalBytes));
      setMaxFileCloudGb(bytesToGb(limitsData.maxFileCloudBytes));
      setQuotaLocalGb(bytesToGb(limitsData.quotaLocalBytes));
      setQuotaCloudGb(bytesToGb(limitsData.quotaCloudBytes));
      setAllowedFileTypes((fileTypesData.allowedExtensions || []).join(", "));
      setAiTopK(String(aiConfigData.topK));
      setAiThreshold(String(aiConfigData.similarityThreshold));
      setAiRecentLimit(String(aiConfigData.recentMessageLimit));
      setAiSubjectOverlapWeight(String(aiConfigData.subjectOverlapWeight));
      setAiFavoriteWeight(String(aiConfigData.favoriteWeight));
      setAiDownloadWeight(String(aiConfigData.downloadWeight));
      setAiRatingWeight(String(aiConfigData.ratingWeight));
      setAiChunkSize(String(aiConfigData.chunkSize));
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

  // Lưu tuổi tối thiểu được phép tải tài liệu lên.
  const saveMinUploadAge = async (value) => {
    setSaving(true);
    try {
      const data = await adminApi.updateMinUploadAge(Number(value));
      setMinUploadAge(String(data.minUploadAge));
      showSuccess(t("admin.settings.saved"));
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
      return t("admin.settings.valMustBeNumber", { label });
    }
    if (num <= 0) {
      return t("admin.settings.valMustBePositive", { label });
    }
    if (num > MAX_LIMIT_GB) {
      return t("admin.settings.valMax", { label, max: MAX_LIMIT_GB });
    }
    return null;
  };

  // Validate rồi quy đổi GB -> byte và lưu giới hạn dung lượng file/quota.
  const saveUploadLimits = async (values) => {
    const { maxFileLocalGbValue, maxFileCloudGbValue, quotaLocalGbValue, quotaCloudGbValue } = values;

    const checks = [
      [maxFileLocalGbValue, t("admin.settings.labelFileLocal")],
      [maxFileCloudGbValue, t("admin.settings.labelFileCloud")],
      [quotaLocalGbValue, t("admin.settings.labelQuotaLocal")],
      [quotaCloudGbValue, t("admin.settings.labelQuotaCloud")],
    ];
    for (const [value, label] of checks) {
      const error = validateGbValue(value, label);
      if (error) {
        showError(error);
        return false;
      }
    }
    if (Number(maxFileLocalGbValue) > Number(quotaLocalGbValue)) {
      showError(t("admin.settings.fileLocalExceedsQuota"));
      return false;
    }
    if (Number(maxFileCloudGbValue) > Number(quotaCloudGbValue)) {
      showError(t("admin.settings.fileCloudExceedsQuota"));
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
      showSuccess(t("admin.settings.saved"));
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

  // Parse chuỗi đuôi tệp admin nhập, validate định dạng, rồi lưu whitelist mới.
  const saveAllowedFileTypes = async (raw) => {
    const extensions = parseExtensions(raw);
    if (extensions.length === 0) {
      showError(t("admin.settings.fileTypesEmpty"));
      return false;
    }
    const invalid = extensions.find((ext) => !/^[a-z0-9]{1,12}$/.test(ext));
    if (invalid) {
      showError(t("admin.settings.extInvalid", { ext: invalid }));
      return false;
    }

    setSavingFileTypes(true);
    try {
      const data = await adminApi.updateUploadFileTypes(extensions);
      setAllowedFileTypes((data.allowedExtensions || []).join(", "));
      showSuccess(t("admin.settings.saved"));
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setSavingFileTypes(false);
    }
  };

  // Validate ở FE trước cho phản hồi tức thì; validate thật (cùng khoảng) ở BE
  // (SystemSettingAdminController#updateAiConfig) vẫn là nguồn sự thật cuối cùng.
  const validateRange = (value, label, min, max) => {
    const num = Number(value);
    if (value === "" || Number.isNaN(num)) {
      return t("admin.settings.valMustBeNumber", { label });
    }
    if (num < min || num > max) {
      return t("admin.settings.valRange", { label, min, max });
    }
    return null;
  };

  // Validate từng tham số AI theo đúng khoảng cho phép rồi lưu.
  const saveAiConfig = async (values) => {
    const {
      topK,
      similarityThreshold,
      recentMessageLimit,
      subjectOverlapWeight,
      favoriteWeight,
      downloadWeight,
      ratingWeight,
      chunkSize,
    } = values;

    const checks = [
      [topK, t("admin.settings.aiTopK"), 1, 20],
      [similarityThreshold, t("admin.settings.aiThreshold"), 0.0, 1.0],
      [recentMessageLimit, t("admin.settings.aiRecentLimit"), 0, 50],
      [subjectOverlapWeight, t("admin.settings.aiSubjectOverlapWeight"), 0, Infinity],
      [favoriteWeight, t("admin.settings.aiFavoriteWeight"), 0, Infinity],
      [downloadWeight, t("admin.settings.aiDownloadWeight"), 0, Infinity],
      [ratingWeight, t("admin.settings.aiRatingWeight"), 0, Infinity],
      [chunkSize, t("admin.settings.aiChunkSize"), 100, 2000],
    ];
    for (const [value, label, min, max] of checks) {
      const error = validateRange(value, label, min, max);
      if (error) {
        showError(error);
        return false;
      }
    }

    setSavingAiConfig(true);
    try {
      const data = await adminApi.updateAiConfig({
        topK: Number(topK),
        similarityThreshold: Number(similarityThreshold),
        recentMessageLimit: Number(recentMessageLimit),
        subjectOverlapWeight: Number(subjectOverlapWeight),
        favoriteWeight: Number(favoriteWeight),
        downloadWeight: Number(downloadWeight),
        ratingWeight: Number(ratingWeight),
        chunkSize: Number(chunkSize),
      });
      setAiTopK(String(data.topK));
      setAiThreshold(String(data.similarityThreshold));
      setAiRecentLimit(String(data.recentMessageLimit));
      setAiSubjectOverlapWeight(String(data.subjectOverlapWeight));
      setAiFavoriteWeight(String(data.favoriteWeight));
      setAiDownloadWeight(String(data.downloadWeight));
      setAiRatingWeight(String(data.ratingWeight));
      setAiChunkSize(String(data.chunkSize));
      showSuccess(t("admin.settings.saved"));
      return true;
    } catch (err) {
      showError(err.message);
      return false;
    } finally {
      setSavingAiConfig(false);
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
    aiTopK,
    aiThreshold,
    aiRecentLimit,
    aiSubjectOverlapWeight,
    aiFavoriteWeight,
    aiDownloadWeight,
    aiRatingWeight,
    aiChunkSize,
    savingAiConfig,
    saveAiConfig,
  };
}
