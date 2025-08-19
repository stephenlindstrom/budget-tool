import { useState, useEffect, useMemo, useRef } from "react";
import { AuthContext } from "./AuthContext";
import { jwtDecode } from "jwt-decode";
import { configureApi } from "../api/api";
import { useNavigate } from "react-router-dom";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);

  const navigate = useNavigate();

  // Keep a live pointer to the latest token so axios always reads the current one
  const tokenRef = useRef(null);
  useEffect(() => { tokenRef.current = token; }, [token]);

  // One-time axios wiring. getToken reads from tokenRef
  useEffect(() => {
    configureApi({
      getToken: () => tokenRef.current,
      onUnauthorized: () => {
        localStorage.removeItem("budget-app-token");
        setToken(null);
        navigate("/login?reason=expired");
      },
    });
  }, [navigate]);
  

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

  useEffect(() => {
    if (!token) {setUser(null); return; }
    try { setUser(jwtDecode(token)); }
    catch { setUser(null); }
  }, [token]);

  const login = (newToken) => {
    localStorage.setItem("budget-app-token", newToken);
    setToken(newToken);
  };

  const logout = () => {
    localStorage.removeItem("budget-app-token");
    setToken(null);
  };

  const value = useMemo(
    () => ({ token, user, login, logout, loading }),
    [token, user, loading]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}