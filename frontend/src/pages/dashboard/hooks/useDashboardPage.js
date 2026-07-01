import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../hooks/useAuth";
import { useToast } from "../../../hooks/useToast";
import { ROUTES } from "../../../constants/routes";

export function useDashboardPage() {
  const { user, logout } = useAuth();
  const { showSuccess } = useToast();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    showSuccess("Đã đăng xuất.");
    navigate(ROUTES.HOME);
  };

  return { user, handleLogout };
}
