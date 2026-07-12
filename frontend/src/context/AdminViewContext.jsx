import { createContext, useCallback, useContext, useMemo, useState } from "react";

const AdminViewContext = createContext(null);

export function AdminViewProvider({ children }) {
  const [viewAsUser, setViewAsUser] = useState(false);

  const toggleViewAsUser = useCallback(() => {
    setViewAsUser((currentValue) => !currentValue);
  }, []);

  const value = useMemo(
    () => ({ viewAsUser, toggleViewAsUser }),
    [viewAsUser, toggleViewAsUser]
  );

  return (
    <AdminViewContext.Provider value={value}>
      {children}
    </AdminViewContext.Provider>
  );
}

export function useAdminView() {
  const context = useContext(AdminViewContext);

  if (!context) {
    throw new Error("useAdminView must be used within <AdminViewProvider>");
  }

  return context;
}
