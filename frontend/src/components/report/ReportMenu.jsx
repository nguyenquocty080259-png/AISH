import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import * as reportApi from "../../api/reportApi";
import { useToast } from "../../hooks/useToast";
import Button from "../ui/Button";
import Modal from "../ui/Modal";
import "./report-menu.css";

export default function ReportMenu({ targetType, targetId }) {
  const { t } = useTranslation();
  const { showError, showSuccess } = useToast();
  const rootRef = useRef(null);
  const [menuOpen, setMenuOpen] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const closeOnOutsideClick = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", closeOnOutsideClick);
    return () => document.removeEventListener("mousedown", closeOnOutsideClick);
  }, []);

  const openReportModal = () => {
    setMenuOpen(false);
    setReason("");
    setModalOpen(true);
  };

  const closeModal = () => {
    if (!submitting) setModalOpen(false);
  };

  const submitReport = async (event) => {
    event.preventDefault();
    const normalizedReason = reason.trim();
    if (!normalizedReason) {
      showError(t("common.report.emptyReason"));
      return;
    }

    setSubmitting(true);
    try {
      await reportApi.createReport({ targetType, targetId, reason: normalizedReason });
      setModalOpen(false);
      setReason("");
      showSuccess(t("common.report.success"));
    } catch (error) {
      showError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="report-menu" ref={rootRef}>
      <button type="button" className="report-menu__trigger"
        onClick={() => setMenuOpen((open) => !open)} aria-label={t("common.report.menuAria")}
        aria-haspopup="menu" aria-expanded={menuOpen}>
        ⋮
      </button>

      {menuOpen && (
        <div className="report-menu__dropdown" role="menu">
          <button type="button" role="menuitem" onClick={openReportModal}>{t("common.report.report")}</button>
        </div>
      )}

      <Modal open={modalOpen} onClose={closeModal} title={t("common.report.modalTitle")} className="report-menu__modal">
        <form onSubmit={submitReport}>
          <label className="report-menu__field">
            {t("common.report.reasonLabel")}
            <textarea rows={4} value={reason} onChange={(event) => setReason(event.target.value)}
              placeholder={t("common.report.reasonPlaceholder")} autoFocus required />
          </label>
          <div className="report-menu__actions">
            <Button variant="secondary" onClick={closeModal} disabled={submitting}>{t("common.actions.cancel")}</Button>
            <Button type="submit" disabled={submitting || !reason.trim()}>
              {submitting ? t("common.actions.sending") : t("common.actions.send")}
            </Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
