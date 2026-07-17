import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import AppRoutes from "./routes/AppRoutes";
import { useToastListener } from "./hooks/useToast";

function ToastStack() {
  const { toasts, dismiss } = useToastListener();
  return (
    <div className="toast-stack">
      {toasts.map((toast) => (
        <div
          key={toast.id}
          className={`toast toast--${toast.type}`}
          onClick={() => dismiss(toast.id)}
        >
          {toast.message}
        </div>
      ))}
    </div>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        {/* Trước AppRoutes để useToastListener đăng ký listener trước khi các trang con
            (vd. OAuthSuccessPage, useLoginPage) gọi showError() ngay trong effect mount đầu tiên -
            thứ tự ngược lại làm mất toast khi vào thẳng trang qua hard redirect từ BE. */}
        <ToastStack />
        <AppRoutes />
      </AuthProvider>
    </BrowserRouter>
  );
}
