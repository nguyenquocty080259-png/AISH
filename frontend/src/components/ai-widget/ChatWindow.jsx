import { useAiWidget } from "../../context/AiWidgetContext";
import ChatHeader from "./ChatHeader";
import MessageList from "./MessageList";
import ChatInput from "./ChatInput";
import HistoryPanel from "./HistoryPanel";
import DocContextBar from "./DocContextBar";
import "./ai-widget.css";

// Kích thước/vị trí cửa sổ do CSS lo (kể cả bố cục màn hẹp), không còn phải đo
// window.innerWidth và re-render theo sự kiện resize.
export default function ChatWindow() {
  const {
    messages,
    isTyping,
    isHistoryLoading,
    isHistoryPanelOpen,
    sendMessage,
  } = useAiWidget();

  return (
    <section className="ai-widget" aria-label="AI HiveMind chat">
      <ChatHeader />
      <div className="ai-widget__body">
        <MessageList
          messages={messages}
          isTyping={isTyping}
          isHistoryLoading={isHistoryLoading}
        />
        {isHistoryPanelOpen && <HistoryPanel />}
      </div>
      <DocContextBar />
      <ChatInput onSend={sendMessage} disabled={isTyping || isHistoryLoading} />
    </section>
  );
}
