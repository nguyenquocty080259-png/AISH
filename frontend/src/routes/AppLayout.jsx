import { Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { ROUTES } from "../constants/routes";
import AIHiveMindWidget from "../components/ai-widget/AIHiveMindWidget";
import GuestNavbar from "../components/guest/GuestNavbar";
import GuestFooter from "../components/guest/GuestFooter";
import AppSidebar from "../components/layout/AppSidebar";
import TopBar from "../components/layout/TopBar";
import { AiWidgetProvider } from "../context/AiWidgetContext";
import "./AppLayout.css";

const AUTH_PATHS = [
  ROUTES.LOGIN, ROUTES.SIGNUP, ROUTES.VERIFY_OTP,
  ROUTES.FORGOT_PASSWORD, ROUTES.RESET_PASSWORD, ROUTES.OAUTH_SUCCESS,
];

export default function AppLayout() {
  const { isAuthenticated } = useAuth();
  const { pathname } = useLocation();
  const showGuestChrome = !isAuthenticated && !AUTH_PATHS.includes(pathname);

  return (
    <AiWidgetProvider>
      <div className="app-shell">
        {isAuthenticated && <AppSidebar />}

        <div className="app-shell__body">
          {isAuthenticated && <TopBar />}
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
