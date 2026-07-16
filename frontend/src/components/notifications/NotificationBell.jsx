import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import * as notificationApi from "../../api/notificationApi";
import { useToast } from "../../hooks/useToast";
import { ROUTES } from "../../constants/routes";
import "./notification-bell.css";

function formatDate(value) {
  return value ? new Date(value).toLocaleString("vi-VN") : "";
}

export default function NotificationBell() {
  const { showError } = useToast();
  const navigate = useNavigate();
  const rootRef = useRef(null);
  const [open, setOpen] = useState(false);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(false);
  const [markingAll, setMarkingAll] = useState(false);

  useEffect(() => {
    let active = true;
    notificationApi.getUnreadCount()
      .then((data) => active && setUnreadCount(Number(data?.count) || 0))
      .catch((error) => active && showError(error.message));
    return () => { active = false; };
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
      setNotifications(await notificationApi.getMyNotifications());
    } catch (error) {
      showError(error.message);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (notification) => {
    try {
      if (!notification.isRead) {
        const updated = await notificationApi.markAsRead(notification.id);
        setNotifications((current) => current.map((item) => item.id === updated.id ? updated : item));
        setUnreadCount((count) => Math.max(0, count - 1));
      }
      if (notification.type === "DOCUMENT_SCREENED" && notification.relatedDocumentId) {
        navigate(`${ROUTES.ADMIN_DOCUMENTS}?needsReview=true`);
      }
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
      <button type="button" className="notification-bell__trigger" onClick={toggleDropdown}
        aria-label={`Thông báo, ${unreadCount} chưa đọc`} aria-expanded={open}>
        <span aria-hidden="true">🔔</span>
        {unreadCount > 0 && <span className="notification-bell__badge">{unreadCount > 99 ? "99+" : unreadCount}</span>}
      </button>

      {open && <div className="notification-bell__dropdown">
        <div className="notification-bell__header">
          <strong>Thông báo</strong>
          <button type="button" onClick={handleMarkAllAsRead}
            disabled={markingAll || unreadCount === 0}>
            {markingAll ? "Đang xử lý..." : "Đánh dấu tất cả đã đọc"}
          </button>
        </div>
        <div className="notification-bell__list">
          {loading ? <p className="notification-bell__empty">Đang tải...</p> :
            notifications.length === 0 ? <p className="notification-bell__empty">Chưa có thông báo.</p> :
            notifications.map((notification) =>
              <button type="button" key={notification.id}
                className={`notification-bell__item${notification.isRead ? "" : " notification-bell__item--unread"}`}
                onClick={() => handleMarkAsRead(notification)}>
                <span>{notification.message}</span>
                <time>{formatDate(notification.createdAt)}</time>
              </button>)}
        </div>
      </div>}
    </div>
  );
}
