import { BrowserRouter, Routes, Route } from "react-router-dom";

import SignUp from "./pages/SignUp";
import OTPVerification from "./pages/OTPVerification";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/signup" element={<SignUp />} />
        <Route
          path="/otp-verification"
          element={<OTPVerification />}
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;