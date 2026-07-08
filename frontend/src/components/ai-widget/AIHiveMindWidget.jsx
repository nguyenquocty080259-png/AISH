import { useLocation } from "react-router-dom";
import { ROUTES } from "../../constants/routes";
import { useAiWidget } from "../../context/AiWidgetContext";
import FloatingButton from "./FloatingButton";
import ChatWindow from "./ChatWindow";

export default function AIHiveMindWidget() {
  const location = useLocation();
  const { isOpen } = useAiWidget();

  if (location.pathname === ROUTES.AI_CHAT) {
    return null;
  }

  return isOpen ? <ChatWindow /> : <FloatingButton />;
}
