import { Routes, Route } from "react-router-dom";

import HomePage from "../pages/home/HomePage";
import Login from "../pages/auth/Login";
import SignUp from "../pages/auth/SignUp";
import NotFoundPage from "../pages/error/NotFoundPage";
import DashboardPage from "../pages/dashboard/dashboard";
import OTPVerification from "../pages/auth/OTPVerification";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<SignUp />} />
      <Route path="/dashboardPage" element={<DashboardPage />} />
      <Route path="/otp-verification" element={<OTPVerification/>} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default AppRoutes;