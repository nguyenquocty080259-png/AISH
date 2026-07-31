import { useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import PageHeader from "../../components/ui/PageHeader";
import Badge from "../../components/ui/Badge";
import Button from "../../components/ui/Button";
import Modal from "../../components/ui/Modal";
import EmptyState from "../../components/ui/EmptyState";
import Table from "../../components/ui/Table";
import Tabs from "../../components/ui/Tabs";
import { SkeletonText } from "../../components/ui/Skeleton";
import CaseThread from "../../components/interaction/CaseThread";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import { resolveRoute } from "../../utils/notificationRoute";
import { useInteractionsPage } from "./hooks/useInteractionsPage";
import "./interactions-page.css";

const REPORT_STATUS_BADGE = {
  PENDING: { intent: "warning", labelKey: "myReports.statusPending" },
  RESOLVED: { intent: "success", labelKey: "myReports.statusResolved" },
  DISMISSED: { intent: "neutral", labelKey: "myReports.statusDismissed" },
};

const APPEAL_STATUS_BADGE = {
  APPEAL_PENDING: { intent: "warning", labelKey: "interactions.statusPendingAppeal" },
  APPEAL_APPROVED: { intent: "success", labelKey: "interactions.statusApprovedAppeal" },
  APPEAL_REJECTED: { intent: "error", labelKey: "interactions.statusRejectedAppeal" },
};

export default function InteractionsPage() {
  const { t, i18n } = useTranslation();
  const { role } = useAuth();
  const isAdmin = role === "ADMIN";
  const { showError } = useToast();
  const navigate = useNavigate();
  const locale = i18n.resolvedLanguage === "en" ? "en-US" : "vi-VN";
  const formatDate = (value) => (value ? new Date(value).toLocaleString(locale) : "—");

  const {
    activeTab,
    setActiveTab,
    summary,
    notifications,
    notificationsLoading,
    notificationsLoadingMore,
    notificationsHasMore,
    loadMoreNotifications,
    markNotificationRead,
    markAllAsRead,
    markingAll,
    deleteNotification,
    clearAllNotifications,
    clearingAll,
    reports,
    reportsLoading,
    appeals,
    appealsLoading,
  } = useInteractionsPage();

  const [thread, setThread] = useState(null);

  const handleNotificationClick = async (notification) => {
    try {
      if (!notification.isRead) await markNotificationRead(notification);
      const path = resolveRoute(notification, isAdmin);
      if (path) navigate(path);
    } catch (error) {
      showError(error.message);
    }
  };

  const handleDeleteNotification = (event, notification) => {
    event.stopPropagation();
    deleteNotification(notification);
  };

  // Tabs dùng chung nhận count là số, tự render chip — không truyền node vào label nữa.
  const tabCount = (count) => (count > 0 ? (count > 99 ? "99+" : count) : undefined);

  return (
    <div className="page-shell interactions-page">
      <PageHeader title={t("interactions.title")} subtitle={t("interactions.subtitle")} />

      <Tabs
        className="interactions-tabs"
        items={[
          { value: "notifications", label: t("interactions.tabNotifications"), count: tabCount(summary?.unreadNotifications) },
          { value: "reports", label: t("interactions.tabReports"), count: tabCount(summary?.myPendingReports) },
          { value: "appeals", label: t("interactions.tabAppeals"), count: tabCount(summary?.myPendingAppeals) },
        ]}
        value={activeTab}
        onChange={setActiveTab}
        ariaLabel={t("interactions.title")}
      />

      {activeTab === "notifications" && (
        <>
          <div className="interactions-page__header-actions">
            <Button
              variant="secondary"
              onClick={markAllAsRead}
              disabled={markingAll || !summary?.unreadNotifications}
            >
              {markingAll ? t("interactions.marking") : t("interactions.markAllRead")}
            </Button>
            <Button
              variant="ghost"
              onClick={clearAllNotifications}
              disabled={clearingAll || notifications.length === 0}
            >
              {clearingAll ? t("interactions.marking") : t("interactions.clearAll")}
            </Button>
          </div>
          {notificationsLoading ? (
            <SkeletonText lines={6} />
          ) : notifications.length === 0 ? (
            <EmptyState icon="🔔" message={t("interactions.emptyNotifications")} />
          ) : (
            <div className="interactions-page__notification-list">
              {notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`interactions-page__notification-item${notification.isRead ? "" : " interactions-page__notification-item--unread"}`}
                >
                  <button
                    type="button"
                    className="interactions-page__notification-content has-custom-focus"
                    onClick={() => handleNotificationClick(notification)}
                  >
                    <span>{notification.message}</span>
                    <time className="interactions-page__notification-time">{formatDate(notification.createdAt)}</time>
                  </button>
                  <button
                    type="button"
                    className="interactions-page__notification-delete has-custom-focus"
                    aria-label={t("interactions.delete")}
                    onClick={(event) => handleDeleteNotification(event, notification)}
                  >
                    ✕
                  </button>
                </div>
              ))}
              {notificationsHasMore && (
                <div className="interactions-page__load-more">
                  <Button variant="secondary" onClick={loadMoreNotifications} disabled={notificationsLoadingMore}>
                    {notificationsLoadingMore ? t("interactions.marking") : t("interactions.loadMore")}
                  </Button>
                </div>
              )}
            </div>
          )}
        </>
      )}

      {activeTab === "reports" && (
        reportsLoading ? (
          <p className="interactions-page__loading">{t("interactions.loadingReports")}</p>
        ) : reports.length === 0 ? (
          <EmptyState icon="📭" message={t("interactions.emptyReports")} />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>{t("myReports.colTarget")}</Table.HeaderCell>
                <Table.HeaderCell>{t("myReports.colId")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colReason")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colStatus")}</Table.HeaderCell>
                <Table.HeaderCell>{t("myReports.colAdminResponse")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colDate")}</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {reports.map((report) => {
                const badge = REPORT_STATUS_BADGE[report.status] ?? REPORT_STATUS_BADGE.PENDING;
                return (
                  <Table.Row key={report.id}>
                    <Table.Cell>{report.targetType}</Table.Cell>
                    <Table.Cell>#{report.targetId}</Table.Cell>
                    <Table.Cell className="ui-table__truncate">{report.reason || "—"}</Table.Cell>
                    <Table.Cell><Badge intent={badge.intent}>{t(badge.labelKey)}</Badge></Table.Cell>
                    <Table.Cell>{report.adminResponse || "—"}</Table.Cell>
                    <Table.Cell>{formatDate(report.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <Button variant="ghost" onClick={() => setThread({ caseType: "report", caseId: report.id })}>
                        {t("interactions.open")}
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        )
      )}

      {activeTab === "appeals" && (
        appealsLoading ? (
          <p className="interactions-page__loading">{t("interactions.loadingAppeals")}</p>
        ) : appeals.length === 0 ? (
          <EmptyState icon="📭" message={t("interactions.emptyAppeals")} />
        ) : (
          <Table>
            <Table.Head>
              <Table.Row>
                <Table.HeaderCell>{t("interactions.colDocument")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colReason")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colStatus")}</Table.HeaderCell>
                <Table.HeaderCell>{t("interactions.colDate")}</Table.HeaderCell>
                <Table.HeaderCell />
              </Table.Row>
            </Table.Head>
            <Table.Body>
              {appeals.map((appeal) => {
                const badge = APPEAL_STATUS_BADGE[appeal.status] ?? APPEAL_STATUS_BADGE.APPEAL_PENDING;
                return (
                  <Table.Row key={appeal.id}>
                    <Table.Cell>#{appeal.documentId}</Table.Cell>
                    <Table.Cell className="ui-table__truncate">{appeal.reason || "—"}</Table.Cell>
                    <Table.Cell><Badge intent={badge.intent}>{t(badge.labelKey)}</Badge></Table.Cell>
                    <Table.Cell>{formatDate(appeal.createdAt)}</Table.Cell>
                    <Table.Cell>
                      <Button variant="ghost" onClick={() => setThread({ caseType: "appeal", caseId: appeal.id })}>
                        {t("interactions.open")}
                      </Button>
                    </Table.Cell>
                  </Table.Row>
                );
              })}
            </Table.Body>
          </Table>
        )
      )}

      <Modal open={!!thread} onClose={() => setThread(null)} title={t("caseThread.title")}>
        {thread && <CaseThread caseType={thread.caseType} caseId={thread.caseId} />}
      </Modal>
    </div>
  );
}
