import { useEffect, useState } from "react";
import { useAiWidget } from "../../context/AiWidgetContext";
import ChatHeader from "./ChatHeader";
import MessageList from "./MessageList";
import ChatInput from "./ChatInput";

const getWindowStyle = (isSmallScreen) => ({
  position: "fixed",
  right: isSmallScreen ? 12 : 24,
  bottom: isSmallScreen ? 12 : 24,
  width: isSmallScreen ? "calc(100vw - 24px)" : 360,
  height: isSmallScreen ? "min(520px, calc(100vh - 24px))" : 480,
  borderRadius: 18,
  overflow: "hidden",
  background: "#fff",
  boxShadow: "0 24px 60px rgba(15, 23, 42, 0.24)",
  border: "1px solid rgba(15, 23, 42, 0.08)",
  zIndex: 9999,
  display: "flex",
  flexDirection: "column",
});

export default function ChatWindow() {
  const { messages, isTyping, isHistoryLoading, sendMessage } = useAiWidget();
  const [isSmallScreen, setIsSmallScreen] = useState(() => window.innerWidth <= 480);

  useEffect(() => {
    const onResize = () => setIsSmallScreen(window.innerWidth <= 480);
    window.addEventListener("resize", onResize);
    return () => window.removeEventListener("resize", onResize);
  }, []);

  return (
    <section style={getWindowStyle(isSmallScreen)} aria-label="AI HiveMind chat">
      <ChatHeader />
      <MessageList messages={messages} isTyping={isTyping} isHistoryLoading={isHistoryLoading} />
      <ChatInput onSend={sendMessage} disabled={isTyping || isHistoryLoading} />
    </section>
  );
}
