import { StrictMode } from "react";
import { createRoot } from "react-dom/client";

import "./assets/css/tailwind.css";
import "./assets/css/reset.css";
import "./styles/tokens.css";
import "./assets/css/global.css";
import "./assets/css/toast.css";

import "./i18n";

import App from "./App.jsx";

createRoot(document.getElementById("root")).render(
  <StrictMode>
    <App />
  </StrictMode>
);
