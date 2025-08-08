import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";
import { jwtDecode } from "jwt-decode";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem("budget-app-token");
    if (storedToken) {
      try {
        const decoded = jwtDecode(storedToken);
        const isExpired = decoded.exp * 1000 < Date.now(); // exp is in seconds, Date.now() is ms
        if (!isExpired) {
          setToken(storedToken);
        } else {
          localStorage.removeItem("budget-app-token");
        }
      } catch (err) {
        console.error("Invalid token:", err);
        localStorage.removeItem("budget-app-token");
      }
    }
    setLoading(false);
  }, []);

  const login = (newToken) => {
    localStorage.setItem("budget-app-token", newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem("budget-app-token");
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ token, login, logout, loading }}>
      {children}
    </AuthContext.Provider>
  );
}