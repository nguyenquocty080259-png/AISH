import { Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { useToast } from "../../hooks/useToast";
import { ROUTES } from "../../constants/routes";
import AppSidebar from "../../components/layout/AppSidebar";
import TopBar from "../../components/layout/TopBar";
import logo from "../../assets/images/hivemind-logo.png";
import { useMemo } from "react";

function I({ children }) { return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{children}</svg>; }
const IconDoc = () => <I><path d="M6 2h8l4 4v16H6z" /><path d="M14 2v4h4" /></I>;
const IconSubject = () => <I><path d="M4 19V5a2 2 0 0 1 2-2h11l3 3v13a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2Z" /><path d="M8 8h6M8 12h8M8 16h5" /></I>;
const IconTag = () => <I><path d="M20 12 12 20l-8-8 8-8h8v8Z" /><circle cx="16" cy="8" r="1.5" /></I>;
const IconUsers = () => <I><circle cx="9" cy="8" r="3" /><path d="M3 21v-1a5 5 0 0 1 5-5h2a5 5 0 0 1 5 5v1" /><path d="M16 6a3 3 0 0 1 0 6" /><path d="M21 21v-1a5 5 0 0 0-3-4.6" /></I>;
const IconFlag = () => <I><path d="M4 21V4h13l-2 4 2 4H4" /></I>;
const IconReport = () => <I><path d="M4 20V10M10 20V4M16 20v-6M2 20h20" /></I>;
const IconChart = () => <I><path d="M3 3v18h18" /><path d="M7 15l4-4 3 3 5-6" /></I>;
const IconSettings = () => <I><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.7 1.7 0 0 0 .3 1.9l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.9-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 0 1-4 0v-.1a1.7 1.7 0 0 0-1-1.5 1.7 1.7 0 0 0-1.9.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.9 1.7 1.7 0 0 0-1.5-1H3a2 2 0 0 1 0-4h.1a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.9l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.9.3 1.7 1.7 0 0 0 1-1.5V3a2 2 0 0 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.9-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.9 1.7 1.7 0 0 0 1.5 1H21a2 2 0 0 1 0 4h-.1a1.7 1.7 0 0 0-1.5 1Z" /></I>;
const IconUser = () => <I><circle cx="12" cy="8" r="4" /><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" /></I>;
const IconSwitch = () => <I><path d="M8 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h3" /><path d="M16 21h3a2 2 0 0 0 2-2V5a2 2 0 0 0-2-2h-3" /><path d="M12 8l-4 4 4 4" /><path d="M8 12h12" /></I>;
const IconLogout = () => <I><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" /></I>;

const ADMIN_NAV_GROUPS = [
  { label: "Content", items: [
    { to: ROUTES.ADMIN_DOCUMENTS, label: "Tài liệu", icon: <IconDoc /> },
    { to: ROUTES.ADMIN_SUBJECTS, label: "Môn học", icon: <IconSubject /> },
    { to: ROUTES.ADMIN_KEYWORDS, label: "Từ khóa", icon: <IconTag /> },
  ] },
  { label: "User", items: [
    { to: ROUTES.ADMIN_USERS, label: "Người dùng", icon: <IconUsers /> },
    { to: ROUTES.ADMIN_APPEALS, label: "Khiếu nại", icon: <IconFlag /> },
    { to: ROUTES.ADMIN_REPORTS, label: "Báo cáo", icon: <IconReport /> },
  ] },
  { label: "System", items: [
    { to: ROUTES.ADMIN_STATS, label: "Thống kê", icon: <IconChart /> },
    { to: ROUTES.ADMIN_SETTINGS, label: "Cấu hình", icon: <IconSettings /> },
  ] },
];

export default function AdminLayout() {
  const { logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };

  const menuItems = useMemo(() => [
    { icon: <IconUser />, label: "Trang cá nhân", onClick: () => navigate(ROUTES.PROFILE), divideAfter: false },
    { icon: <IconSwitch />, label: "Xem như User", onClick: () => navigate(ROUTES.DASHBOARD), divideAfter: true },
    { icon: <IconLogout />, label: "Đăng xuất", onClick: handleLogout },
  ], [navigate]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <div className="flex min-h-screen bg-surface">
      <AppSidebar
        brand={{ logoSrc: logo, name: "HiveMind", caption: "ADMIN", homeTo: ROUTES.ADMIN }}
        navGroups={ADMIN_NAV_GROUPS}
      />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopBar left="Chế độ Quản trị" menuItems={menuItems} />
        <main className="flex-1"><Outlet /></main>
      </div>
    </div>
  );
}
