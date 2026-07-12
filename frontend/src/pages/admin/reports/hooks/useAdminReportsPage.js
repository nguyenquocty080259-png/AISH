import { useEffect, useState } from "react";
import * as adminApi from "../../../../api/adminApi";
import { useToast } from "../../../../hooks/useToast";

export function useAdminReportsPage() {
  const { showError, showSuccess } = useToast();
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState("");
  const [resolveTarget, setResolveTarget] = useState(null);
  const [actionTaken, setActionTaken] = useState("");
  const [adminResponse, setAdminResponse] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let active = true;
    setLoading(true);
    adminApi.listReports(statusFilter || undefined)
      .then((data) => active && setReports(data))
      .catch((error) => active && showError(error.message))
      .finally(() => active && setLoading(false));
    return () => { active = false; };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [statusFilter]);

  const openResolveModal = (report) => {
    setResolveTarget(report);
    setActionTaken("");
    setAdminResponse("");
  };
  const closeResolveModal = () => { if (!submitting) setResolveTarget(null); };
  const submitResolve = async () => {
    if (!resolveTarget || !actionTaken) return showError("Vui lòng chọn hành động xử lý.");
    setSubmitting(true);
    try {
      const updated = await adminApi.resolveReport(resolveTarget.id, {
        actionTaken, adminResponse: adminResponse.trim() || null,
      });
      setReports((current) => statusFilter && updated.status !== statusFilter
        ? current.filter((report) => report.id !== updated.id)
        : current.map((report) => report.id === updated.id ? updated : report));
      setResolveTarget(null);
      showSuccess("Đã xử lý báo cáo.");
    } catch (error) {
      showError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return { reports, loading, statusFilter, setStatusFilter, resolveTarget,
    actionTaken, setActionTaken, adminResponse, setAdminResponse, submitting,
    openResolveModal, closeResolveModal, submitResolve };
}
