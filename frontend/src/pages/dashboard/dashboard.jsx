import { useAuth } from "./AuthContext";
import DashboardLayout from "./DashboardLayout";
import StudentDashboard from "./StudentDashboard";
import AdminDashboard from "./AdminDashboard";

// ============================================================
// Dashboard — router theo role
// Không cần chỉnh file này khi thêm role mới,
// chỉ cần thêm vào ROLE_MAP bên dưới
// ============================================================

const ROLE_MAP = {
  student: StudentDashboard,
  admin:   AdminDashboard,
};

function LoadingScreen() {
  return (
    <div style={{
      height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      gap: 12, background: "#F8F9FA", fontFamily: "'DM Sans', sans-serif",
    }}>
      <div style={{
        width: 36, height: 36, borderRadius: 10,
        background: "#E08307", color: "#fff",
        display: "flex", alignItems: "center", justifyContent: "center",
        fontSize: 18, fontWeight: 700,
      }}>A</div>
      <p style={{ fontSize: 14, color: "#544435" }}>Đang tải...</p>
    </div>
  );
}

function UnauthorizedScreen() {
  return (
    <div style={{
      height: "100vh", display: "flex", flexDirection: "column",
      alignItems: "center", justifyContent: "center",
      gap: 12, background: "#F8F9FA", fontFamily: "'DM Sans', sans-serif",
    }}>
      <p style={{ fontSize: 32 }}>🔒</p>
      <h2 style={{ fontSize: 18, fontWeight: 700, color: "#191C1D" }}>Không có quyền truy cập</h2>
      <p style={{ fontSize: 14, color: "#544435" }}>Role của bạn không được hỗ trợ.</p>
      <a href="/login" style={{
        marginTop: 8, padding: "9px 20px", borderRadius: 10,
        background: "#E08307", color: "#fff", textDecoration: "none",
        fontSize: 13, fontWeight: 600,
      }}>Đăng nhập lại</a>
    </div>
  );
}

export default function Dashboard() {
  const { role, loading } = useAuth();

  if (loading) return <LoadingScreen />;

  const RoleContent = ROLE_MAP[role];

  if (!RoleContent) return <UnauthorizedScreen />;

  return (
    <DashboardLayout>
      <RoleContent />
    </DashboardLayout>
  );
}