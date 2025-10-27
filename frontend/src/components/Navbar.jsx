import { useAuth } from "../hooks/useAuth";
import { useNavigate } from "react-router-dom";
import PennyPincherLogo from "./PennyPincherLogo";

export default function Navbar() {
  const { logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <nav className="bg-slate-800 shadow-sm">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
        {/* Left side: Brand + links */}
        <div className="flex items-center gap-6">
          <PennyPincherLogo size={42} showWordmark={false} />
          <span
            onClick={() => navigate("/")}
            className="cursor-pointer text-lg font-bold text-white hover:text-blue-300"
          >
            Penny Pincher
          </span>
          <button
            onClick={() => navigate("/")}
            className="text-sm font-medium text-slate-200 transition hover:text-white cursor-pointer"
          >
            Dashboard
          </button>
          <button
            onClick={() => navigate("/transactions")}
            className="text-sm font-medium text-slate-200 transition hover:text-white cursor-pointer"
          >
            Transactions
          </button>
          <button
            onClick={() => navigate("/budgets")}
            className="text-sm font-medium text-slate-200 transition hover:text-white cursor-pointer"
          >
            Budgets
          </button>
        </div>

        {/* Right side: Logout */}
        <div>
          <button
            onClick={handleLogout}
            className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-semibold text-white shadow-sm transition hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500/20 cursor-pointer"
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}