import { Routes, Route } from "react-router-dom";

import HomePage from "../pages/home/HomePage";
import Login from "../pages/auth/Login";
import SignUp from "../pages/auth/SignUp";
import NotFoundPage from "../pages/error/NotFoundPage";
import DashboardPage from "../pages/dashboard/dashboard";

function AppRoutes() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/Login" element={<Login />} />
      <Route path="/sign" element={<SignUp />} />
      <Route path="/DashboardPage" element={<DashboardPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}

export default AppRoutes;