import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import * as notificationApi from "../../../api/notificationApi";
import * as reportApi from "../../../api/reportApi";
import * as appealApi from "../../../api/appealApi";
import * as interactionApi from "../../../api/interactionApi";
import { useToast } from "../../../hooks/useToast";

const VALID_TABS = new Set(["notifications", "reports", "appeals"]);

export function useInteractionsPage() {
  const { showError } = useToast();
  const [searchParams, setSearchParams] = useSearchParams();
  const initialTab = searchParams.get("tab");
  const [activeTab, setActiveTabState] = useState(
    VALID_TABS.has(initialTab) ? initialTab : "notifications"
  );

  const [summary, setSummary] = useState(null);

  const [notifications, setNotifications] = useState([]);
  const [notificationsLoading, setNotificationsLoading] = useState(false);
  const [notificationsLoadingMore, setNotificationsLoadingMore] = useState(false);
  const [notificationsPage, setNotificationsPage] = useState(0);
  const [notificationsHasMore, setNotificationsHasMore] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);

  const [reports, setReports] = useState([]);
  const [reportsLoading, setReportsLoading] = useState(false);

  const [appeals, setAppeals] = useState([]);
  const [appealsLoading, setAppealsLoading] = useState(false);

  useEffect(() => {
    interactionApi.getInteractionSummary().then(setSummary).catch((error) => showError(error.message));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadNotifications = () => {
    setNotificationsLoading(true);
    return notificationApi.getMyNotifications(0, 15)
      .then((data) => {
        setNotifications(data.content ?? []);
        setNotificationsPage(0);
        setNotificationsHasMore(!data.last);
      })
      .catch((error) => showError(error.message))
      .finally(() => setNotificationsLoading(false));
  };

  const loadMoreNotifications = () => {
    const nextPage = notificationsPage + 1;
    setNotificationsLoadingMore(true);
    return notificationApi.getMyNotifications(nextPage, 15)
      .then((data) => {
        setNotifications((current) => [...current, ...(data.content ?? [])]);
        setNotificationsPage(nextPage);
        setNotificationsHasMore(!data.last);
      })
      .catch((error) => showError(error.message))
      .finally(() => setNotificationsLoadingMore(false));
  };

  const loadReports = () => {
    setReportsLoading(true);
    return reportApi.getMyReports()
      .then(setReports)
      .catch((error) => showError(error.message))
      .finally(() => setReportsLoading(false));
  };

  const loadAppeals = () => {
    setAppealsLoading(true);
    return appealApi.getMyAppeals()
      .then(setAppeals)
      .catch((error) => showError(error.message))
      .finally(() => setAppealsLoading(false));
  };

  useEffect(() => {
    if (activeTab === "notifications") loadNotifications();
    else if (activeTab === "reports") loadReports();
    else if (activeTab === "appeals") loadAppeals();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab]);

  const setActiveTab = (tab) => {
    setActiveTabState(tab);
    const next = new URLSearchParams(searchParams);
    if (tab === "notifications") next.delete("tab");
    else next.set("tab", tab);
    setSearchParams(next, { replace: true });
  };

  const markNotificationRead = async (notification) => {
    if (notification.isRead) return notification;
    const updated = await notificationApi.markAsRead(notification.id);
    setNotifications((current) => current.map((item) => (item.id === updated.id ? updated : item)));
    setSummary((current) =>
      current ? { ...current, unreadNotifications: Math.max(0, current.unreadNotifications - 1) } : current
    );
    return updated;
  };

  const markAllAsRead = async () => {
    setMarkingAll(true);
    try {
      await notificationApi.markAllAsRead();
      setNotifications((current) => current.map((item) => ({ ...item, isRead: true })));
      setSummary((current) => (current ? { ...current, unreadNotifications: 0 } : current));
    } catch (error) {
      showError(error.message);
    } finally {
      setMarkingAll(false);
    }
  };

  return {
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
    reports,
    reportsLoading,
    appeals,
    appealsLoading,
  };
}
