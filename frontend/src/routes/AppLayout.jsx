import { useMemo } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../hooks/useAuth";
import { useToast } from "../hooks/useToast";
import { ROUTES } from "../constants/routes";
import { ROLES } from "../constants/roles";
import AIHiveMindWidget from "../components/ai-widget/AIHiveMindWidget";
import GuestNavbar from "../components/guest/GuestNavbar";
import GuestFooter from "../components/guest/GuestFooter";
import AppSidebar from "../components/layout/AppSidebar";
import TopBar from "../components/layout/TopBar";
import { AiWidgetProvider } from "../context/AiWidgetContext";
import logo from "../assets/images/hivemind-logo.png";
import "./AppLayout.css";

// Icon nhỏ nội bộ, KHÔNG import ở file khác — tránh phình.
function I({ children }) { return <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">{children}</svg>; }
const IconHome = () => <I><path d="M3 9.5 12 3l9 6.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1V9.5Z" /></I>;
const IconDoc = () => <I><path d="M6 2h8l4 4v16H6z" /><path d="M14 2v4h4" /></I>;
const IconUsers = () => <I><circle cx="9" cy="8" r="3" /><path d="M3 21v-1a5 5 0 0 1 5-5h2a5 5 0 0 1 5 5v1" /><path d="M16 6a3 3 0 0 1 0 6" /><path d="M21 21v-1a5 5 0 0 0-3-4.6" /></I>;
const IconCollection = () => <I><path d="M4 4h16v14l-8-4-8 4z" /></I>;
const IconHeart = () => <I><path d="M12 21s-7-4.5-9.5-9A5 5 0 0 1 12 6a5 5 0 0 1 9.5 6c-2.5 4.5-9.5 9-9.5 9Z" /></I>;
const IconShare = () => <I><circle cx="18" cy="5" r="3" /><circle cx="6" cy="12" r="3" /><circle cx="18" cy="19" r="3" /><path d="M8.6 13.5l6.8 4M15.4 6.5l-6.8 4" /></I>;
const IconAi = () => <I><path d="M12 3v3M12 18v3M3 12h3M18 12h3" /><circle cx="12" cy="12" r="4" /></I>;
const IconReport = () => <I><path d="M4 20V10M10 20V4M16 20v-6M2 20h20" /></I>;
const IconTrash = () => <I><path d="M3 6h18M8 6V4h8v2M6 6l1 14h10l1-14" /></I>;
const IconPlus = () => <I><path d="M12 5v14M5 12h14" /></I>;
const IconUser = () => <I><circle cx="12" cy="8" r="4" /><path d="M4 21v-1a6 6 0 0 1 6-6h4a6 6 0 0 1 6 6v1" /></I>;
const IconShield = () => <I><path d="M12 3l7 4v5c0 4-3 7-7 9-4-2-7-5-7-9V7l7-4Z" /></I>;
const IconLogout = () => <I><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><path d="M16 17l5-5-5-5" /><path d="M21 12H9" /></I>;
const AUTH_PATHS = [
  ROUTES.LOGIN, ROUTES.SIGNUP, ROUTES.VERIFY_OTP,
  ROUTES.FORGOT_PASSWORD, ROUTES.RESET_PASSWORD, ROUTES.OAUTH_SUCCESS,
];

export default function AppLayout() {
  const { t } = useTranslation();
  const { isAuthenticated, user, role, logout } = useAuth();
  const { pathname } = useLocation();
  const showGuestChrome = !isAuthenticated && !AUTH_PATHS.includes(pathname);
  const navigate = useNavigate();
  const { showSuccess } = useToast();

  const userNavGroups = useMemo(() => [{
    items: [
      { to: ROUTES.DASHBOARD, label: t("nav.dashboard"), icon: <IconHome /> },
      { to: ROUTES.DOCUMENTS, label: t("nav.myDocuments"), icon: <IconDoc /> },
      { to: ROUTES.COMMUNITY, label: t("nav.community"), icon: <IconUsers /> },
      { to: ROUTES.SPACES, label: t("nav.collections"), icon: <IconCollection /> },
      { to: ROUTES.FAVORITES, label: t("nav.favorites"), icon: <IconHeart /> },
      { to: ROUTES.SHARED_WITH_ME, label: t("nav.sharedWithMe"), icon: <IconShare /> },
      { to: ROUTES.AI_CHAT, label: t("nav.aiHiveMind"), icon: <IconAi /> },
    ],
  }, {
    items: [
      { to: ROUTES.MY_REPORTS, label: t("nav.myReports"), icon: <IconReport /> },
      { to: ROUTES.TRASH, label: t("nav.trash"), icon: <IconTrash /> },
    ],
  }], [t]);

  const handleLogout = async () => { await logout(); showSuccess(t("common.loggedOut")); navigate(ROUTES.HOME); };
  const userMenuItems = useMemo(() => [
    { icon: <IconUser />, label: t("common.menu.profile"), onClick: () => navigate(ROUTES.PROFILE), divideAfter: role !== ROLES.ADMIN },
    ...(role === ROLES.ADMIN ? [{ icon: <IconShield />, label: t("common.menu.backToAdmin"), onClick: () => navigate(ROUTES.ADMIN), divideAfter: true }] : []),
    { icon: <IconLogout />, label: t("common.menu.logout"), onClick: handleLogout },
  ], [role, navigate, t]); // eslint-disable-line react-hooks/exhaustive-deps

  return (
    <AiWidgetProvider>
      <div className="app-shell">
        {isAuthenticated && <AppSidebar
          brand={{ logoSrc: logo, name: "HiveMind", caption: "AI STUDY HUB", homeTo: ROUTES.HOME }}
          navGroups={userNavGroups}
          bottomAction={{ label: t("nav.addDocument"), to: ROUTES.DOCUMENTS, icon: <IconPlus /> }}
        />}

        <div className="app-shell__body">
          {isAuthenticated && <TopBar menuItems={userMenuItems} />}
          {showGuestChrome && <GuestNavbar />}

          <main className="app-main">
            <Outlet />
          </main>

          {showGuestChrome && <GuestFooter />}
        </div>
      </div>
      {isAuthenticated && <AIHiveMindWidget />}
    </AiWidgetProvider>
  );
}
