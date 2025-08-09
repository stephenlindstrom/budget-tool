import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/");
  };

  return (
    <nav style={{ padding: "1rem", background: "#eee" }}>
      <button onClick={() => navigate("/dashboard")}>Dashboard</button>
      <button onClick={handleLogout} style={{ marginLeft: "1rem" }}>
        Logout
      </button>
    </nav>
  );
}