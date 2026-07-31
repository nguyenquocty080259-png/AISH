import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import * as notificationApi from "../../api/notificationApi";
import { useToast } from "../../hooks/useToast";
import { useAuth } from "../../hooks/useAuth";
import { ROUTES } from "../../constants/routes";
import { resolveRoute } from "../../utils/notificationRoute";
import "./notification-bell.css";

// Chu kỳ poll badge unread-count, ~30-60s.
const UNREAD_POLL_MS = 45000;

export default function NotificationBell() {
  const { t, i18n } = useTranslation();
  const formatDate = (value) =>
    value ? new Date(value).toLocaleString(i18n.language === "en" ? "en-US" : "vi-VN") : "";
  const { showError } = useToast();
  const { role } = useAuth();
  const isAdmin = role === "ADMIN";
  const navigate = useNavigate();
  const rootRef = useRef(null);
  const [open, setOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);

  useEffect(() => {
    let active = true;
    const fetchUnreadCount = () => {
      notificationApi.getUnreadCount()
        .then((data) => active && setUnreadCount(Number(data?.count) || 0))
        .catch(() => {});
    };
    notificationApi.getUnreadCount()
      .then((data) => active && setUnreadCount(Number(data?.count) || 0))
      .catch((error) => active && showError(error.message));
    const intervalId = setInterval(fetchUnreadCount, UNREAD_POLL_MS);
    return () => { active = false; clearInterval(intervalId); };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    const closeOnOutsideClick = (event) => {
      if (rootRef.current && !rootRef.current.contains(event.target)) setOpen(false);
    };
    document.addEventListener("mousedown", closeOnOutsideClick);
    return () => document.removeEventListener("mousedown", closeOnOutsideClick);
  }, []);

  const toggleDropdown = async () => {
    const nextOpen = !open;
    setOpen(nextOpen);
    if (!nextOpen) return;
    setLoading(true);
    try {
      const data = await notificationApi.getMyNotifications(0, 15);
      setNotifications(data.content ?? []);
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleViewAll = () => {
    setOpen(false);
    navigate(`${ROUTES.INTERACTIONS}?tab=notifications`);
  };

  const handleMarkAsRead = async (notification) => {
    try {
      if (!notification.isRead) {
        const updated = await notificationApi.markAsRead(notification.id);
        setNotifications((current) => current.map((item) => item.id === updated.id ? updated : item));
        setUnreadCount((count) => Math.max(0, count - 1));
      }
      const path = resolveRoute(notification, isAdmin);
      if (path) navigate(path);
    } catch (error) {
      showError(error.message);
    }
  };

  const handleMarkAllAsRead = async () => {
    setMarkingAll(true);
    try {
      await notificationApi.markAllAsRead();
      setNotifications((current) => current.map((item) => ({ ...item, isRead: true })));
      setUnreadCount(0);
    } catch (error) {
      showError(error.message);
    } finally {
      setMarkingAll(false);
    }
  };

  return (
    <div className="notification-bell" ref={rootRef}>
      <button type="button" className="notification-bell__trigger has-custom-focus" onClick={toggleDropdown}
        aria-label={t("common.notifications.ariaLabel", { count: unreadCount })} aria-expanded={open}>
        <span aria-hidden="true">🔔</span>
        {unreadCount > 0 && <span className="notification-bell__badge">{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>

      {open && <div className="notification-bell__dropdown">
        <div className="notification-bell__header">
          <strong>{t("common.notifications.title")}</strong>
          <button type="button" onClick={handleMarkAllAsRead}
            disabled={markingAll || unreadCount === 0}>
            {markingAll ? t("common.notifications.marking") : t("common.notifications.markAllRead")}
          </button>
        </div>
        <div className="notification-bell__list">
          {loading ? <p className="notification-bell__empty">{t("common.notifications.loading")}</p> :
            notifications.length === 0 ? <p className="notification-bell__empty">{t("common.notifications.empty")}</p> :
            notifications.map((notification) =>
              <button type="button" key={notification.id}
                className={`notification-bell__item has-custom-focus${notification.isRead ? "" : " notification-bell__item--unread"}`}
                onClick={() => handleMarkAsRead(notification)}>
                <span>{notification.message}</span>
                <time>{formatDate(notification.createdAt)}</time>
              </button>)}
        </div>
        <div className="notification-bell__footer">
          <button type="button" onClick={handleViewAll}>{t("common.notifications.viewAll")}</button>
        </div>
      </div>}
    </div>
  );
}
