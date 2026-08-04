import { useLocation } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import { useAiWidget } from "../../context/AiWidgetContext";
import FloatingButton from "./FloatingButton";
import ChatWindow from "./ChatWindow";

// WIDGET CHAT AI NỔI — hiển thị ở MỌI trang (trừ chính trang AI Chat toàn màn hình, tránh trùng
// lặp 2 khung chat). Đóng thì hiện nút tròn nổi (FloatingButton), mở thì hiện cửa sổ chat (ChatWindow).
export default function AIHiveMindWidget() {
  const location = useLocation();
  const { isOpen } = useAiWidget();

  if (location.pathname === ROUTES.AI_CHAT) {
    return null;
  }

  return isOpen ? <ChatWindow /> : <FloatingButton />;
}
