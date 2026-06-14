import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import DocumentDetailPage from './pages/document/DocumentDetailPage';
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import OTPVerificationPage from './pages/auth/OTPVerificationPage';
import DocumentPage from './pages/document/DocumentPage';
import CloudStoragePage from './pages/cloud/CloudStoragePage';

function App() {
  return (
    <AuthProvider>
      <div className="App">
        <Routes>
          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/otp-verification" element={<OTPVerificationPage />} />

          {/* DocumentPage làm trang chính sau login */}
          <Route path="/dashboard" element={<DocumentPage />} />
          <Route path="/documents" element={<DocumentPage />} />
          <Route path="/documents/:id" element={<DocumentDetailPage />} />
          <Route path="/cloud-storage" element={<CloudStoragePage />} />

          {/* Catch-all phải nằm CUỐI CÙNG */}
          <Route path="*" element={<div style={{padding:20}}>404 - Không có trang: {window.location.pathname}</div>} />
        </Routes>
      </div>
    </AuthProvider>
  );
}

export default App;